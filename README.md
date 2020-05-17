# KalahChessGame
How to run our agent against localhost 12345 (human).

  Firstly run javac MKAgent/Main.java in the src folder
  Secondly run nc -l localhost 12345 in a seperate terminal
  Thirdly run java -jar ManKalah.jar "nc localhost 12345" "java MKAgent/Main" in the src folder

DONT FORGET TO RECOMPILE Main after any changes are made!!

Useful commands:
	java -jar ManKalah.jar "java MKAgent/TestBot 1 3 4 1 2 3 1 4 2 6 7 7 6 5 6 4 3 6 5 2 6 5 3 2 4 5 6 1 4" "java MKAgent/Main"
	java -jar ManKalah.jar "java MKAgent/Main" "java MKAgent/TestBot 1 4 3 5 3 1 2 6 7 1 2 7 6 7 5 6 7 5 7 3 7 5 7 2 1 7 2 6 7 5 6"
	java -jar ManKalah.jar "java MKAgent/Main" "java -jar ../Agents/error404.jar"
	java -jar ManKalah.jar "java -jar ../Agents/error404.jar" "java MKAgent/Main"
