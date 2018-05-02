# ESP32 Beacon finder with Firebase backend
This is the install guide to let your ESP32 find beacons in De Krook. The application separates the beacons from other bluetooth devices (hardcoded). 
### Install Arduino IDE & Espressif
You need to install Aruindo IDE to connect with your ESP32 and the [Espressif framework](https://github.com/espressif/arduino-esp32). 
### Change partitions 
The application and his used libraries are too large to write to your ESP32P program storage. So you need to change that partition: 

1.  Make a copy of `esp32/tools/partitions/default.csv` as backup
2.  Open `default.csv` with your favorite text-editor. 
3.  Delete everything, paste the lines below and save it. 
```
# Name,   Type, SubType, Offset,  Size, Flags
nvs, data, nvs, 0x9000, 0x5000,
otadata, data, ota, 0xe000, 0x2000,
app0, app, ota_0, 0x10000, 0x1C0000,
app1, app, ota_1, 0x1D0000,0x1C0000,
eeprom, data, 0x99, 0x390000,0x1000,
spiffs, data, spiffs, 0x391000,0x6F000,
```  
4.  Open `esp32/board.txt` with a text-editor.
5.  Make the following changes: 

    `esp32.upload.maximum_size=1310720` to `1835008`  
    `esp32doit-devkit-v1.upload.maximum_size=1310720` to `1835008`
6. Save it and restart Arduino IDE. 

### Program setup

1. Make a new Firebase project
2. Open the program file with Arduino IDE
3. Change the `firebaseLink` to your obtained firebase URL.
4. Change the WIFI settings




