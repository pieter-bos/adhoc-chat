Wireshark Protocol Dissector
============================

You can add the cacbdsdcbiccda protocol to wireshark by adding a line to ```/etc/wireshark/init.lua```:

    dofile("/path/to/file/cacbdsdcbiccda.lua")

Then start wireshark **as non-root** and go to ```Edit -> Preferences -> Protocols -> DLT_USER -> New``` and fill in the following values:

    Payload protocol: caa
    Header size: 0
    Trailer size: 0
