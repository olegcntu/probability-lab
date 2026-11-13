@echo off
chcp 65001 >nul
cd /d "%~dp0"
set JAR=probability-lab.jar
if not exist "%JAR%" set JAR=target\probability-lab.jar
if not exist "%JAR%" (
  echo Не знайдено файл probability-lab.jar. Зберіть проєкт командою: mvn package
  pause
  exit /b 1
)
where java >nul 2>nul
if errorlevel 1 (
  echo Не знайдено Java. Встановіть Java 17 або новішу: https://adoptium.net
  pause
  exit /b 1
)
echo ==============================================================
echo  Лабораторія ймовірностей
echo  Автор: Красавін Олег Олександрович, 2 курс магістратури, група 51-фмт
echo  НУЧК імені Т. Г. Шевченка, 2026
echo ==============================================================
echo Запуск програми... Браузер відкриється автоматично.
echo Щоб зупинити програму, закрийте це вікно.
start "" cmd /c "timeout /t 7 >nul & start http://localhost:8080"
java -jar "%JAR%"
pause
