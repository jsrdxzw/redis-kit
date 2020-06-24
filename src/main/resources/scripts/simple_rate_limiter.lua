local key = KEYS[1]
if key ~= nil then
    local current = tonumber(redis.call('GET', key))
    local limit = tonumber(ARGV[1])
    local expire = tonumber(ARGV[2])
    if current == nil or current <= limit then
        local v = redis.call('INCR', key)
        if v == 1 then
            redis.call('EXPIRE', key, expire)
        end
        return true
    end
end
return false