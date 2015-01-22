# Android App to configure Wifi via NFC

A "back-port" to Android 4.4 of the functionality from 5.0 to use a NFC tag to add/modify credentials for a Wifi SSID to an android device.

## Usage

Standard (**not** Mifare) NFC Forum Type2 ISO14443A tags (eg. NXP NTAG203) can be used.

The Wifi Configuration data can be written to Tags using either the [NXP TagWriter app](https://play.google.com/store/apps/details?id=com.nxp.nfc.tagwriter) or from the Android 5.0 Wifi settings by long-pressing on Wifi network in the list of saved Wifi SSIDs **with** stored Credentials.

Only using the NXP TagWriter app has currently been tested.


## License

NfcUtils class is Copyright (C) 2014 The Android Open Source Project under the Apache 2.0 License.

All other source and documentation is:

Copyright (C) 2015 Maksim Lin

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
