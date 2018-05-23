# ESP32 tracking 
ESP32's kunnen gevolgd worden in de bibliotheek door middel van dit project. Het programma zoekt naar bluetooth toestellen en filtert hardcoded de beacons van de bibliotheek eruit. 

### Installeer Arduino IDE & Espressif
De Arduino IDO en het [Espressif framework](https://github.com/espressif/arduino-esp32) moet geinstalleerd worden om verbinding te maken met de ESP32. 

### Herpartitioneren
Doordat een aantal grote libraries gebruikt zijn is het programmageheugen van de ESP32 te klein. Dit kan opgelost worden door het opnieuw partioneren van het toestel.

1. Maak een kopie van `esp32/tools/partitions/default.csv` als backup
2. Open `default.csv` met een text-editor
3. Wis alles, plak onderstaande lijnen en sla het bestand op. 
```
# Name,   Type, SubType, Offset,  Size, Flags
nvs, data, nvs, 0x9000, 0x5000,
otadata, data, ota, 0xe000, 0x2000,
app0, app, ota_0, 0x10000, 0x1C0000,
app1, app, ota_1, 0x1D0000,0x1C0000,
eeprom, data, 0x99, 0x390000,0x1000,
spiffs, data, spiffs, 0x391000,0x6F000,
```  
4. Open `esp32/board.txt` met een text-editor
5. Verander volgende lijnen in het bestand:  
    `esp32.upload.maximum_size=1310720` naar `esp32.upload.maximum_size=1835008`  
    `esp32doit-devkit-v1.upload.maximum_size=1310720` naar `esp32doit-devkit-v1.upload.maximum_size=13107201835008`
6. Sla het bestand op en herstart de Arduino IDE

### Programma installatie

1. Maak een nieuw, of gebruik een bestaand Firebase project.
2. Open het [programmabestand](https://github.com/lab9k/Beacons/blob/master/Web/ESP32/beaconFirebase/beaconFirebase.ino) met Arduino IDE. 
3. Verander de `firebaseLink` naar de URL van uw Firebase project. (meestal `https://[applicatienaam].firebaseio.com`)
4. Verander de WIFI instellingen naar die van de bibliotheek. 




