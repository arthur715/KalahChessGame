#! /bin/bash

numberOfTimes=$1

if [ $2 = "-jar" ] ; then
    jarFlag1=$2
    lastest_version=$3

    if [ $4 = "-jar" ] ; then
      jarFlag2=$4
      random_move_version=$5
    else
      jarFlag2=""
      random_move_version=$4
    fi
else
    jarFlag1=""
    lastest_version=$2

    if [ $3 = "-jar" ] ; then
      jarFlag2=$3
      random_move_version=$4
    else
      jarFlag2=""
      random_move_version=$3
    fi
fi


((southRunCount=1))
((southWinCount=0))
((southLoseCount=0))
((southTotalTimePerMove=0))
((southAverageTimePerMove=0))

((northRunCount=1))
((northWinCount=0))
((northLoseCount=0))
((northTotalTimePerMove=0))
((northAverageTimePerMove=0))

((line=0))
((side=1)) # SOUTH = 0; NORTH = 1

> statistic.txt

if [ $# -ge 3 ]; then

    ################## play south ##################

      echo "////////////////////// latest version : $lastest_version play South //////////////////////" >> statistic.txt #latest version play south
      echo "////////////////////// latest version : $lastest_version play South //////////////////////"
      while [ $southRunCount -le $numberOfTimes ] ; do
          echo "########### $southRunCount  run ###########"
          java -jar ManKalah.jar "java $jarFlag1 $lastest_version" "java $jarFlag2 $random_move_version" >> statistic.txt
          ((southRunCount++))
      done
      ((southRunCount--))
      echo "///////////////////// Total $southRunCount run completed /////////////////////////"

    ################## play north ##################

      echo "////////////////////// latest version : $lastest_version play North //////////////////////" >> statistic.txt #latest version play south
      echo "////////////////////// latest version : $lastest_version play North //////////////////////"
      while [ $northRunCount -le $numberOfTimes ] ; do
          echo "########### $northRunCount  run ###########"
          java -jar ManKalah.jar "java $jarFlag2 $random_move_version" "java $jarFlag1 $lastest_version" >> statistic.txt
          ((northRunCount++))
      done
      ((northRunCount--))
      echo "///////////////////// Total $northRunCount run completed /////////////////////////"

    ################## process data ##################
    ((line++))

    while read winOrLoseString averageTimePerMoveString; do

        if [ $winOrLoseString = "//////////////////////" ] ; then

            if [ $side -eq 0 ] ; then
              ((side=1))
            elif [ $side -eq 1 ] ; then
              ((side=0))
            fi

            continue
        else
            if [ $side -eq 0 ] ; then #if SOUTH; then
                if [ $line -eq 1 ] ; then

                    if [ $winOrLoseString -eq 1 ] ; then
                      ((southWinCount++))
                      ((southTotalTimePerMove+=averageTimePerMoveString))
                    elif [ $winOrLoseString -eq 0 ] ; then
                      ((southLoseCount++))
                      ((southTotalTimePerMove+=averageTimePerMoveString))
                    fi

                    ((line++))
                else
                    ((line--))
                    continue
                fi
            else
                if [ $line -eq 1 ] ; then
                    ((line++))
                    continue
                else
                    if [ $winOrLoseString -eq 1 ] ; then
                      ((northWinCount++))
                      ((northTotalTimePerMove+=averageTimePerMoveString))
                    elif [ $winOrLoseString -eq 0 ] ; then
                      ((northLoseCount++))
                      ((northTotalTimePerMove+=averageTimePerMoveString))
                    fi
                    ((line--))
                fi
            fi
        fi
    done < statistic.txt

    ###################### print statistics ######################
    echo "////////////////////////////////// statistics ////////////////////////////////////////" >> statistic.txt
    southAverageTimePerMove=$((southTotalTimePerMove/southRunCount))
    echo "latest version $lastest_version is South: " >> statistic.txt
    echo "    win = $southWinCount; lose = $southLoseCount; average time per move = $southAverageTimePerMove " >> statistic.txt

    northAverageTimePerMove=$((northTotalTimePerMove/northRunCount))
    echo "latest version $lastest_version is North: " >> statistic.txt
    echo "    win = $northWinCount; lose = $northLoseCount; average time per move = $northAverageTimePerMove " >> statistic.txt


else
    echo "Please give the number of times you want to execute the game!"
    echo "Usage : java -jar ManKalah.jar <number of times to run> <latest version> <randome move version>"
fi
