#!/usr/bin/python3
# -*- coding: utf-8 -*-
# Author:Mine DOGAN <mine.dogan@agem.com.tr>

import xml.etree.ElementTree as ET

from base.model.enum.ContentType import ContentType
from base.plugin.abstract_plugin import AbstractPlugin


class ScanNetwork(AbstractPlugin):
    def __init__(self, task, context):
        super(AbstractPlugin, self).__init__()
        self.task = task
        self.context = context
        self.logger = self.get_logger()
        self.message_code = self.get_message_code()

        self.command = 'nmap -oX - ' + self.task['ipRange']
        self.logger.debug('[NETWORK INVENTORY] Initialized')

    def handle_task(self):
        try:
            self.logger.debug('[NETWORK INVENTORY] Scanning')
            result_code, p_out, p_err = self.execute(self.command)

            allLines = [line for line in str(p_out).splitlines()]
            data_no_firsttwo = "".join(map(str, allLines[2:]))

            root = ET.fromstringlist(data_no_firsttwo)

            data = self.get_result(root)
            print(data)

            self.context.create_response(code=self.message_code.TASK_PROCESSED.value,
                                         message='User NETWORK INVENTORY task processed successfully',
                                         data=data, content_type=ContentType.APPLICATION_JSON.value)
            self.logger.info('[NETWORK INVENTORY] NETWORK INVENTORY task is handled successfully')
        except Exception as e:
            self.logger.error(
                '[NETWORK INVENTORY] A problem occured while handling NETWORK INVENTORY task: {0}'.format(str(e)))
            self.context.create_response(code=self.message_code.TASK_ERROR.value,
                                         message='A problem occured while handling NETWORK INVENTORY task: {0}'.format(
                                             str(e)))

    def get_result(self, root):
        self.logger.debug('[NETWORK INVENTORY] Parsing nmap xml output')
        result_list = []

        for host in root.findall('host'):
            result = {}

            hostnames = host.find('hostnames')
            ports = host.find('ports')
            os = host.find('os')
            distance = host.find('distance')

            result['hostnames'] = self.get_hostname_list(hostnames)
            result['ports'] = self.get_port_list(ports)
            result['os'] = self.get_os_list(os)
            result['distance'] = self.get_distance(distance)
            result['ipAddress'], result['macAddress'], result['macProvider'] = self.get_Addresses(host)
            result['time'] = self.get_time()

            result_list.append(result)

        return result_list

    def get_Addresses(self, host):
        ipAddress = ''
        macAddress = ''
        macProvider = ''
        if host != None:
            for address in host.findall('address'):
                if address.get('addrtype') == 'ipv4':
                    ipAddress = address.get('addr')
                if address.get('addrtype') == 'mac':
                    macAddress = address.get('addr')
                    macProvider = address.get('vendor')
        return ipAddress, macAddress, macProvider

    def get_hostname_list(self, hostnames):
        hostname_list = []
        if hostnames != None:
            for hostname in hostnames.findall('hostname'):
                name = hostname.get('name')
                hostname_list.append(name)
        return hostname_list

    def get_port_list(self, ports):
        port_list = []
        if ports != None:
            for port in ports.findall('port'):
                service = port.find('service')
                service_name = service.get('name')
                id = port.get('portid') + '/' + port.get('protocol') + ' ' + service_name
                port_list.append(id)
        return port_list

    def get_os_list(self, os):
        os_list = []
        if os != None:
            for osmatch in os.findall('osmatch'):
                name = osmatch.get('name')
                os_list.append(name)
        return os_list

    def get_distance(self, distance):
        if distance != None:
            return distance.get('value')
        return ''

    def get_time(self):
        # TODO
        return ''


def handle_task(task, context):
    scan = ScanNetwork(task, context)
    scan.handle_task()
