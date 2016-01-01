#! /bin/sh

# Travis OpenJDK workaround
cat /etc/hosts # optionally check the content *before*
hostname "$(hostname | cut -c1-63)"
sed -e "s/^\\(127\\.0\\.0\\.1.*\\)/\\1 $(hostname | cut -c1-63)/" /etc/hosts | tee /etc/hosts
cat /etc/hosts # optionally check the content *after*
