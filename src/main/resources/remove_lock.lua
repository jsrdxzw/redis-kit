local lockClientId = redis.call('GET', KEYS[1])
if lockClientId == ARGV[1] then
    redis.call('DEL', KEYS[1])
end
