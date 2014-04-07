#!/bin/bash 
essid=groep
mac=f8:d1:11:64:5e:
len=$(expr length $2)
if [ $len -eq 2 ]
then
    mac+="$2"
else 
    mac+="a$2"
fi

essid+="$2"
ch=$2
if [ $2 -gt 13 ]
then 
    let "ch = $2 - 13"
fi 

ifconfig $1 down
iwconfig $1 mode ad-hoc
iwconfig $1 channel $ch
iwconfig $1 ap $mac 
iwconfig $1 essid $essid 
iwconfig $1 key 1234567890
ifconfig $1 192.168.5.$3 up
