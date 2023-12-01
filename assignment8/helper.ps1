# Helper script for running the Assignment 4
# Author: Swajeet Swarnkar

function cnd_compile_all {
  javac -cp ./src/ -d ./out/ ./src/com/sg/server/Server.java ./src/com/sg/client/Client.java
}

function cnd_compile_server {
  javac -cp ./src/ -d ./out/ ./src/com/sg/server/Server.java
}

function cnd_compile_client {
  javac -cp ./src/ -d ./out/ ./src/com/sg/client/Client.java
}

function cnd_run_server {
  java -cp ./out/ com.sg.server.Server
}

function cnd_run_client {
  param (
    $HostName
  )
  if ([string]::IsNullOrEmpty($HostName)) {
    java -cp ./out/ com.sg.client.Client "127.0.0.1"
  } else {
    java -cp ./out/ com.sg.client.Client $HostName
  }
}

function cnd_clean {
    rm -ErrorAction SilentlyContinue -Recurse ./out
}

echo "Environment ready."
echo ""
echo "Available commands-"
echo "  cnd_compile_all         : Compiles client and server."
echo "  cnd_compile_server      : Compiles only server."
echo "  cnd_compile_client      : Compiles only client."
echo "  cnd_run_server          : Runs server."
echo "  cnd_run_client [ip]     : Runs client on specified IP or on 127.0.0.1 if not given."
echo "  cnd_clean               : Cleans the project folder, deletes all class files."
echo ""
echo "Author @Swajeet Swarnkar"
