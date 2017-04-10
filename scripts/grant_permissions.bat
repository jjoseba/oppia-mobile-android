set adb=%1
set package=%2

if "%2"=="" (
	echo "No parameters found, run with sh grant_permissions.sh <path_to_adb> <package>"
	exit 
)

"%adb%" devices | findstr /v "List of devices"> test2.txt
set /p devices=<test2.txt
del test2.txt 
echo %devices%

FOR /f %%i IN ("%devices%") DO (
	echo Setting permissions to device %%i for package %package% 
    "%adb%" -s %%i shell pm grant %package% android.permission.READ_PHONE_STATE
    "%adb%" -s %%i shell pm grant %package% android.permission.WRITE_EXTERNAL_STORAGE
    "%adb%" -s %%i shell pm grant %package% android.permission.SET_ANIMATION_SCALE
)	