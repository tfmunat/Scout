n=100
s=15
e=100
t=2000
repeats=10
p=g4
m=sparse_landmarks
em=random_enemymap
fps=500

all: compile

compile:
	javac scout/sim/Simulator.java

gui:
	java scout.sim.Simulator --fps ${fps} --gui -p ${p} -m ${m} -em ${em} -n ${n} -e ${e} -s ${s} -t ${t}

run:
	java scout.sim.Simulator -r ${repeats} -p ${p} -m ${m} -em ${em} -n ${n} -e ${e} -s ${s} -t ${t}

verbose:
	java scout.sim.Simulator -p ${p} -m ${m} -em ${em} -n ${n} -e ${e} -s ${s} -t ${t} --verbose
  
clean:
	find . -name \*.class -type f -delete