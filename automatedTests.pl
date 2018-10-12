#!/usr/bin/perl

# Demo script to automatically start project sample1 several times with
# a different set of node speeds and node densities. Note that this script
# needs severe adaptations to be used for any real-life Sinalgo project. 

# Usage:         Copy this script in the ROOT DIRECTORY OF YOUR SINALGO
#                installation and execute it.
# Requirements:  A working perl installation, e.g. download from http://www.perl.org/
# Hints:         If you are new to perl, a good starting point may be 
#                http://perldoc.perl.org

use strict;
use warnings;

my $numRepetitions = 100; # Number of rounds to perform for each simulation
my $refreshRate = 10;     # Refresh rate
my @failureDetectionModelSuccessRate = (-1.0, 0.95, 1.0);
my @numNodesList = (36, 49, 64, 81, 100);

for my $numNodes (@numNodesList) {
    for my $detectionRate (@failureDetectionModelSuccessRate) {
        for (my $i = 0; $i < $numRepetitions; $i += 1) {
            die "Terminated prematurely" unless
                system("./gradlew run -PappArgs=\"[" .
                    "'-project', 'tcc', " .                                                                 # choose the project
                    "'-gen', '$numNodes', 'tcc:SensorNode', 'tcc:ListBasedPositionModel', 'NoMobility', " . # generate nodes
                    "'-gen', '1', 'tcc:SinkNode', 'tcc:ListBasedPositionModel', 'NoMobility', " .           # generate nodes
                    "'-overwrite', 'failureDetectionModelSuccessRate=$detectionRate', " .                   # Detection rate
                    "'exitOnTerminationInGUI=true', " .                                                     # Close GUI when hasTerminated() returns true
                    "'outputToConsole=false', " .                                                           # Create a framework log-file for each run
                    "'-refreshRate', '$refreshRate', " .                                                    # Don't draw GUI often
                    "'-batch']\" > ~/saida-rssf/sim\\ $numNodes\\ $detectionRate\\ $i.txt"                  # Use batch
                ) == 0;
        }
    }
}
