@echo off
echo Generating release upload key for DompetMax...
keytool -genkeypair -v -storetype PKCS12 -keystore my-upload-key.jks -keysize 2048 -keyalg RSA -validity 10000 -alias upload
echo Done. File my-upload-key.jks generated in root directory.
pause
