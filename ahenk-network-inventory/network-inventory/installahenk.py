#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author:Mine DOGAN <mine.dogan@agem.com.tr>

import paramiko

from base.plugin.abstract_plugin import AbstractPlugin


class InstallAhenk(AbstractPlugin):
    def __init__(self, task, context):
        super(AbstractPlugin, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        # for remote machine that doesn't have ahenk
        self.host = self.task['host']
        self.username = self.task['username']
        self.password = self.task['password']
        self.deb_path = self.task['path']
        self.deb_name = self.task['name']

        self.deb_remote_path = '/home/' + self.username + '/' + self.deb_name
        self.command = 'sudo gdebi -n ' + self.deb_remote_path

        self.logger.debug('[NETWORK INVENTORY - installahenk command] Initialized')

    def handle_task(self):
        try:
            ssh = paramiko.SSHClient()
            ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
            ssh.connect(self.host, username=self.username, password=self.password)
            transport = ssh.get_transport()
            session = transport.open_session()
            session.set_combine_stderr(True)
            session.get_pty()

            self.logger.debug('[NETWORK INVENTORY - installahenk command] SSH connection is started.')

            sftp = ssh.open_sftp()
            sftp.put(self.deb_path, self.deb_remote_path)

            session.exec_command(self.command)
            stdin = session.makefile('wb', -1)
            stdout = session.makefile('rb', -1)

            # you have to check if you really need to send password here
            stdin.write(self.password + '\n')
            stdin.flush()

            self.logger.debug('[NETWORK INVENTORY - installahenk command] Ahenk is installing.')
            print(stdout.read())

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='User NETWORK INVENTORY task processed successfully')
            self.logger.info('[NETWORK INVENTORY] NETWORK INVENTORY task is handled successfully')
        except Exception as e:
            self.logger.error(
                '[NETWORK INVENTORY] A problem occured while handling NETWORK INVENTORY task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='A problem occured while handling NETWORK INVENTORY task: {0}'.format(
                                             str(e)))


def handle_task(task, context):
    install = InstallAhenk(task, context)
    install.handle_task()
