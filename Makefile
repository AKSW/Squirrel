test-static-seed: compile
	java -cp ./target/squirrel-0.1-jar-with-dependencies.jar org.aksw.simba.squirrel.cli.StaticSeedGeneratorCli "ipc://localhost.ipc" &
	java -cp ./target/squirrel-0.1-jar-with-dependencies.jar org.aksw.simba.squirrel.cli.WorkerCli "1" "ipc://localhost.ipc" "ipc://localhostsink.ipc" &
	java -cp ./target/squirrel-0.1-jar-with-dependencies.jar org.aksw.simba.squirrel.cli.ZeroMQBasedFrontierCli "ipc://localhost.ipc" 

compile:
	mvn clean compile assembly:single
