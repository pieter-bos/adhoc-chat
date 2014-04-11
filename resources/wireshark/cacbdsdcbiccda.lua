caa = Proto("CAA", "CAA Protocol")

function caa.dissector(buffer, pinfo, tree)
    pinfo.cols.protocol = "CAA"

    local subtree = tree:add(caa, buffer(), "CAA Protocol Data")

    subtree:add(buffer(0, 1), "Flags: " .. buffer(0, 1):uint())
    subtree:add(buffer(1, 4), "Sequence Number: " .. buffer(1, 4):uint())
    subtree:add(buffer(5, 4), "Acknowledgement Number" .. buffer(5, 4):uint())
end

udp_table = DissectorTable.get("udp.port")
udp_table:add(1234, caa)