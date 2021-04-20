# TCP Multi Chat

### Description
This is java made, multi-user chat server.
Server socket is open at port 8888 when ChatServer.jar file is run.
Clients can connect and use the server for message communication.
All users will receive notification every time another user logs in or logs out.
Since this is was a training exercise, client side connects to 'localhost' by default.

### Usage
User is prompted to choose a username.
Any message is broadcasted by default.
/list  ->  shows all logged in users.
@username message  ->  sends 'message' to user 'username'.
/quit  ->  shuts down connection and closes application.