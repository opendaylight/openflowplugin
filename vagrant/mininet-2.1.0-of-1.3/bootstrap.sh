#!/usr/bin/env bash

#apt-get update
apt-get install -y mininet
cd /;patch -p0 < /vagrant/node.py.patch;cd -

