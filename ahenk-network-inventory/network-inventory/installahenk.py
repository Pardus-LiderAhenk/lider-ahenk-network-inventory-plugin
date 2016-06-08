#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author:Mine DOGAN <mine.dogan@agem.com.tr>

import paramiko
import urllib.request

from base.plugin.abstract_plugin import AbstractPlugin


class InstallAhenk(AbstractPlugin):
    def __init__(self, task, context):
        super(AbstractPlugin, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.access_method = self.task['accessMethod']
        self.install_method = self.task['installMethod']
        self.ipList = self.task['ipList']
        self.username = self.task['username']

        if self.access_method == 'USERNAME_PASSWORD':
            self.password = self.task['password']

        if self.install_method == 'APT_GET':
            self.install_command = 'sudo apt-get install -y --force-yes nmap'  # TODO name for ahenk

        elif self.install_method == 'WGET':
            self.download_url = self.task['downloadUrl']

        self.deb_path = '/tmp/ahenk.deb'
        self.command = 'gdebi -n {}'.format(self.deb_path)

        self.logger.debug('[NETWORK INVENTORY - installahenk command] Initialized')

    def handle_task(self):
        try:
            if self.access_method == 'USERNAME_PASSWORD':
                for i, val in enumerate(self.ipList):
                    self.use_username_password(val)
            elif self.access_method == 'PRIVATE_KEY':
                for i, val in enumerate(self.ipList):
                    self.use_key(val)

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='User NETWORK INVENTORY task processed successfully')
            self.logger.info('[NETWORK INVENTORY] NETWORK INVENTORY task is handled successfully')
        except Exception as e:
            self.logger.error(
                '[NETWORK INVENTORY] A problem occured while handling NETWORK INVENTORY task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='A problem occured while handling NETWORK INVENTORY task: {0}'.format(
                                             str(e)))

    def use_username_password(self, host):
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(host, username=self.username, password=self.password)
        transport = ssh.get_transport()
        session = transport.open_session()
        session.set_combine_stderr(True)
        session.get_pty()

        self.logger.debug('[NETWORK INVENTORY - installahenk command] SSH connection has been started.')

        if self.install_method == 'WGET':
            urllib.request.urlretrieve(self.download_url, self.deb_path)
            sftp = ssh.open_sftp()
            sftp.put(self.deb_path, self.deb_path)
            session.exec_command(self.command)

        elif self.install_method == 'APT_GET':
            session.exec_command(self.install_command)

        stdout = session.makefile('rb', -1)

        self.logger.debug('[NETWORK INVENTORY - installahenk command] Ahenk has been installed.')

        print(stdout.read())

    def use_key(self, host):
        ssh = paramiko.SSHClient()
        ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh.connect(host, username=self.username)

        self.logger.debug('[NETWORK INVENTORY - installahenk command] SSH connection has been started.')

        if self.install_method == 'WGET':
            urllib.request.urlretrieve(self.download_url, self.deb_path)
            sftp = ssh.open_sftp()
            sftp.put(self.deb_path, self.deb_path)
            stdin, stdout, stderr = ssh.exec_command(self.command)

        elif self.install_method == 'APT_GET':
            stdin, stdout, stderr = ssh.exec_command(self.install_command)

        self.logger.debug('[NETWORK INVENTORY - installahenk command] Ahenk has been installed.')

        print(stdout.read())

        self.logger.debug('[NETWORK INVENTORY - installahenk command] Ahenk has been installed.')


def handle_task(task, context):
    install = InstallAhenk(task, context)
    install.handle_task()
