#! /bin/bash

agentListFile=$1

if [ $2 = "-jar" ] ; then
    jarFlag=$2
    lastest_version=$3
else
    jarFlag=""
    lastest_version=$2
fi



> statistic.txt

((numberOfAgents=0))

echo "////////////////////// latest version : $lastest_version is SOUTH //////////////////////"
echo "////////////////////// latest version : $lastest_version is SOUTH //////////////////////" >> statistic.txt

while read agentName jarAgent ; do
  if [ $agentName = "-jar" ] ; then
    echo "NORTH: $jarAgent" >> statistic.txt
    echo "////////////////// SOUTH: $lastest_version vs NORTH: $jarAgent //////////////////"
    java -jar ManKalah.jar "java $jarFlag $lastest_version" "java -jar $jarAgent" >> statistic.txt
  elif [ $agentName = "-exe" ] ; then
    echo "NORTH: $jarAgent" >> statistic.txt
    echo "////////////////// SOUTH: $lastest_version vs NORTH: $jarAgent //////////////////"
    java -jar ManKalah.jar "java $jarFlag $lastest_version" "$jarAgent" >> statistic.txt
  else
    echo "NORTH: $agentName" >> statistic.txt
    echo "////////////////// SOUTH: $lastest_version vs NORTH: $agentName //////////////////"
    java -jar ManKalah.jar "java $jarFlag $lastest_version" "java $agentName" >> statistic.txt
  fi
done < $agentListFile

echo "////////////////////// latest version : $lastest_version is NORTH //////////////////////"
echo "////////////////////// latest version : $lastest_version is NORTH //////////////////////" >>statistic.txt

while read agentName jarAgent ; do
  if [ $agentName = "-jar" ] ; then
    echo "SOUTH: $jarAgent" >> statistic.txt
    echo "////////////////// SOUTH: $jarAgent vs NORTH: $lastest_version //////////////////"
    java -jar ManKalah.jar "java -jar $jarAgent" "java $jarFlag $lastest_version" >> statistic.txt
  elif [ $agentName = "-exe" ] ; then
    echo "SOUTH: $jarAgent" >> statistic.txt
    echo "////////////////// SOUTH: $jarAgent vs NORTH: $lastest_version //////////////////"
    java -jar ManKalah.jar "$jarAgent" "java $jarFlag $lastest_version">> statistic.txt
  else
    echo "SOUTH: $agentName" >> statistic.txt
    echo "////////////////// SOUTH: $agentName vs NORTH: $lastest_version //////////////////"
    java -jar ManKalah.jar "java $agentName" "java $jarFlag $lastest_version" >> statistic.txt
  fi
  ((numberOfAgents++))
done < $agentListFile

echo "FINISH" >> statistic.txt
echo "////////////////////// Run against $numberOfAgents agents completed //////////////////////"

######################## process data ##############################
((line=0))
((latest_version_AverageTimePerMove=0))
((old_version_AverageTimePerMove=0))
((old_version_WinOrLose=-1))
((side=0))
((numberOfGame=0))
((winCount=0))
((loseCount=0))
echo "############################ Tables ############################" >> statistic.txt
while read winOrLoseString averageTimePerMoveString ; do
    if [ $winOrLoseString = "FINISH" ] ; then
      echo " " >> statistic.txt
      echo "Total number of game play: $numberOfGame ; win: $winCount ; lose: $loseCount " >> statistic.txt
      break
    elif [ $winOrLoseString = "//////////////////////" ] ; then
        if [ $side = 0 ] ; then
            echo " " >> statistic.txt
            echo "$lastest_version is SOUTH:" >> statistic.txt
            echo " " >> statistic.txt
            echo "Old version name | Average Time Per Move | Latest version Average Time Per Move | win/lose against old version" >> statistic.txt
            ((side=1))
        elif [ $side = 1 ] ; then
            echo " " >> statistic.txt
            echo "Total number of game play: $numberOfGame ; win: $winCount ; lose: $loseCount " >> statistic.txt
            ((numberOfGame=0))
            ((winCount=0))
            ((loseCount=0))
            echo " " >> statistic.txt
            echo " " >> statistic.txt
            echo " " >> statistic.txt
            echo "$lastest_version is NORTH:" >> statistic.txt
            echo " " >> statistic.txt
            echo "Old version name | Average Time Per Move | Latest version Average Time Per Move | win/lose against old version" >> statistic.txt
            ((side=0))
        fi
    elif [ $winOrLoseString = "NORTH:" ] ; then
        nameOfAgent=$averageTimePerMoveString
        ((line=1))
        ((numberOfGame++))
    elif [ $winOrLoseString = "SOUTH:" ] ; then
        nameOfAgent=$averageTimePerMoveString
        ((line=3))
        ((numberOfGame++))
    else
        if [ $line -eq 1 ] ; then
            ((latest_version_AverageTimePerMove=averageTimePerMoveString))
            ((line++))
        elif [ $line -eq 2 ] ; then
            ((old_version_WinOrLose=winOrLoseString))
            ((old_version_AverageTimePerMove=averageTimePerMoveString))
            ((line=0))
            if [ $old_version_WinOrLose -eq 0 ] ; then
                ((winCount++))
                echo "$nameOfAgent | $old_version_AverageTimePerMove | $latest_version_AverageTimePerMove | win" >>statistic.txt
            elif [ $old_version_WinOrLose -eq 1 ] ; then
                ((loseCount++))
                echo "$nameOfAgent | $old_version_AverageTimePerMove | $latest_version_AverageTimePerMove | lose" >> statistic.txt
            fi
        elif [ $line -eq 3 ] ; then
            ((old_version_WinOrLose=winOrLoseString))
            ((old_version_AverageTimePerMove=averageTimePerMoveString))
            ((line++))
        elif [ $line -eq 4 ] ; then
            ((latest_version_AverageTimePerMove=averageTimePerMoveString))
            ((line=0))
            if [ $old_version_WinOrLose -eq 0 ] ; then
                ((winCount++))
                echo "$nameOfAgent | $old_version_AverageTimePerMove | $latest_version_AverageTimePerMove | win" >> statistic.txt
            elif [ $old_version_WinOrLose -eq 1 ] ; then
                ((loseCount++))
                echo "$nameOfAgent | $old_version_AverageTimePerMove | $latest_version_AverageTimePerMove | lose" >> statistic.txt
            fi
        fi
    fi
done < statistic.txt
