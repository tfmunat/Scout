n=100
s=15
e=700
t=2000
repeats=10
p=g4
m=sparse_landmarks
em=g4
fps=1000

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
