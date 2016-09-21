#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author: Caner Feyzullahoglu <caner.feyzullahoglu@agem.com.tr>
"""
Style Guide is PEP-8
https://www.python.org/dev/peps/pep-0008/
"""

from base.plugin.abstract_plugin import AbstractPlugin


class GetFile(AbstractPlugin):
    def __init__(self, task, context):
        super(AbstractPlugin, self).__init__()

        self.logger = self.get_logger()
        self.logger.debug('[NETWORK INVENTORY] Initialized')
        self.task = task
        self.context = context
        self.message_code = self.get_message_code()

    def handle_task(self):
        parameter_map = self.task
        self.logger.debug('[NETWORK INVENTORY] Handling task')

        self.logger.debug('[NETWORK INVENTORY] Fetching file from: {0} to {1}'.format(parameter_map['remotePath'],
                                                                                      parameter_map['localPath']))

        try:
            self.context.fetch_file(parameter_map['remotePath'], local_path=parameter_map['localPath'],
                                    file_name=parameter_map['fileName'])

            self.logger.debug('[NETWORK INVENTORY] Creating response')
            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='NETWORK INVENTORY dosya paylaşım görevi başarıyla çalıştırıldı.')

        except Exception as e:
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='NETWORK INVENTORY dosya paylaşım görevi çalıştırılırken hata oluştu.')


def handle_task(task, context):
    scan = GetFile(task, context)
    scan.handle_task()
