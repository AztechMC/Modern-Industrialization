# Adding, modifying and removing motors
You will need to use `MIRegistrationEvents.registerMotors` startup event:
```js
MIRegistrationEvents.registerMotors(event => {
    event.register("minecraft:hopper", 1)
    event.modify("modern_industrialization:large_motor", 32)
    event.remove("modern_industrialization:motor")
})
```

`event.register` and `event.modify` take upgrade speed in items per 3 seconds.