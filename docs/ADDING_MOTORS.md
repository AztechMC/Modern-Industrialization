# Adding motors
Motors in MI can be used to upgrade item pipe's extract throughput.

To add, modify or remove motors using KubeJS, you will need to use a startup script and the event `MIRegistrationEvents.registerMotors`.
## Adding a motor
Example script that makes Hopper add +1 Item per 3 seconds:
```js
MIRegistrationEvents.registerMotors(event => {
    event.register("minecraft:hopper", 1)
})
```
## Modifying a motor
Example script that makes Large Motor add +32 Items per 3 seconds:
```js
MIRegistrationEvents.registerMotors(event => {
    event.register("modern_industrialization:large_motor", 32)
})
```
## Removing a motor
Example script that makes Advanced Motor no longer a motor:
```js
MIRegistrationEvents.registerMotors(event => {
    event.remove("modern_industrialization:advanced_motor")
})
```