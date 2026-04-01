# Event Command

> [!IMPORTANT]
> `/event <option> <storage> <path> <function>`<br>

Creates an event which listens for wichever event you specify in `<option>`<br> then stores the relevant data and executes a function when that event fires

---
### \<OPTION>
`chat`: Listens for when a chat message is sent, stores the message in the "message" property and executes the function as the sender

---
### \<STORAGE> \<PATH>
Stores the animations data in said storage, at said path<br>
works like storing data with the `/data` command

---
### \<FUNCTION>
Executes the specified function when the event is fired (this happens after the data is stored in the storage)