# Networking and Security
CLIENT COMMAND(Windows): java -cp "C:\...\testerQ.jar" -D"file.encoding=UTF8" testerq.client.Main localhost 3000

CLIENT COMMAND(unix/mac): java -cp /.../testerQ.jar testerq.client.Main localhost 3000

SERVER COMMAND(Windows): java -cp "C:\...\testerQ.jar" testerq.server.Main 3000

SERVER COMMAND(unix/mac): java -cp /.../testerQ.jar testerq.server.Main 3000

run SERVER COMMAND on port of choice

run CLIENT COMMAND pointing to host and port of server

ON CLIENT:
Enter username when prompted

Choose avatar when prompted

when in game world issue commands such as "move north" OR "mv up 3" OR "mv east 5" this moves around the world

If you enter a space with an I, you should be moved to a new map zone

You should be able to observe a primitive event log below the rendered map