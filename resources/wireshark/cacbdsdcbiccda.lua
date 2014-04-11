caa = Proto("CAA", "CAA Protocol")

function caa.dissector(buffer, pinfo, tree)
    pinfo.cols.protocol = "CAA"

    local subtree = tree:add(caa, buffer(), "CAA Protocol Data")

    subtree:add(buffer(0, 1), "Flags: " .. buffer(0, 1):bitfield())
    subtree:add(buffer(1, 4), "Sequence Number: " .. buffer(1, 4):uint())
    subtree:add(buffer(5, 4), "Acknowledgement Number: " .. buffer(5, 4):uint())
    subtree:add(buffer(9, 4), "Retransmission Number: " .. buffer(9, 4):uint())
    subtree:add(buffer(13, 4), "Source address: " .. buffer(13, 1):uint() .. "." .. buffer(14, 1):uint() .. "." .. buffer(15, 1):uint() .. "." .. buffer(16, 1):uint())
    subtree:add(buffer(17, 4), "Destination address: " .. buffer(17, 1):uint() .. "." .. buffer(18, 1):uint() .. "." .. buffer(19, 1):uint() .. "." .. buffer(20, 1):uint())
    subtree:add(buffer(21, buffer:len() - 21), "Payload")
end

udp_table = DissectorTable.get("udp.port")
udp_table:add(1234, caa)