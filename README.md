
# react-native-allmax-keyboard

## Getting started

`$ npm install react-native-allmax-keyboard --save`

### Mostly automatic installation

`$ react-native link react-native-allmax-keyboard`

### Manual installation


#### iOS

1. In XCode, in the project navigator, right click `Libraries` ➜ `Add Files to [your project's name]`
2. Go to `node_modules` ➜ `react-native-allmax-keyboard` and add `RNAllmaxKeyboard.xcodeproj`
3. In XCode, in the project navigator, select your project. Add `libRNAllmaxKeyboard.a` to your project's `Build Phases` ➜ `Link Binary With Libraries`
4. Run your project (`Cmd+R`)<

#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.reactlibrary.RNAllmaxKeyboardPackage;` to the imports at the top of the file
  - Add `new RNAllmaxKeyboardPackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-allmax-keyboard'
  	project(':react-native-allmax-keyboard').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-allmax-keyboard/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-allmax-keyboard')
  	```

## Usage
```javascript
import { TextField, setHeight } from 'react-native-allmax-keyboard';

...
<TextField
  ref={(ref) => {
    this.scoutInput = ref;
  }}
  onSuccessInstall={() => {
    this.setState({ androidPreloader: false });
  }}
  keyboardHeight={keyboardHeight}
  mountInterval={80}
  customKeyboardType={'NumPad'}
  placeholder="Barcode"
  returnKeyType="done"
  selectionColor={'rgba(0, 0, 0, .5)'}
  spellCheck={false}
  onSubmitEditing={() => {
    setHeight(findNodeHandle(this.scoutInput.textInputRef()), 0);
    if (isFetched !== 1) {
      return fetchByBarcode();
    } else if (isFetched === 1) {
      return setBarcode('');
    }
    return null;
  }}
  onChangeText={(newBarcode) => {
    setBarcode(newBarcode);
  }}
  value={newBarcode}
  style={styles.scan_textInput}
  touchOnFocus={() => {
    if (keyboardHeight === 0 && !this.state.androidPreloader) {
      setKeyboardHeight(this.keyboardHeight);
    }
  }}
  onChangeKeyboardHeight={(height) => {
    if (keyboardHeight !== height) {
      setKeyboardHeight(height);
    }
  }}
  underlineColorAndroid="transparent"
  blurOnSubmit={!externalScanner}
  dismissKeyboardAction={setKeyboardHeight}
/>
...

```

## Variables
```
touchOnFocus: () => {}, // Input is touched
onSuccessInstall: () => {}, // Callback when keyboard is mounted
mountInterval: 80, // Interval to mount keyboard, only for iOS
keyboardHeight: 0, // Keyboard height, default is 0
```

## Functions to use
```
registerKeyboard, // Register in root container
TextField, // TextInput
backSpace, // Delete action
insertText, // Insert action
done, // Done action
clear, // Clear all nodes, only for Android
installKeyboard, // Don't use it
switchSystemKeyboard, // Switch to system keyboard action
setHeight // Set keyboard height
```
  
