import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { findNodeHandle, NativeEventEmitter, NativeModules, TextInput, Alert } from 'react-native';

const { installKeyboard, uninstall, setHeight } = NativeModules.RNAllmaxKeyboard;

let keyboardManagerEmitter;
keyboardManagerEmitter = new NativeEventEmitter(NativeModules.RNAllmaxKeyboard);


export default class TextField extends Component {

  static propTypes = {
    ...TextInput.propTypes,
    customKeyboardType: PropTypes.string.isRequired,
    mountInterval: PropTypes.number,
    touchOnFocus: PropTypes.func,
    onChangeKeyboardHeight: PropTypes.func,
    onSuccessInstall: PropTypes.func,
    keyboardHeight: PropTypes.number,
  };

  static defaultProps = {
    touchOnFocus: () => {},
    onSuccessInstall: () => {},
    mountInterval: 80,
    keyboardHeight: 0,
  };

  constructor (props) {
    super(props);
    this.subscription = keyboardManagerEmitter.addListener(
      'TouchDownTextField',
      this.touchDownTextField
    );
    this.subscription = keyboardManagerEmitter.addListener(
      'onChangeKeyboardHeight',
      this.onChangeKeyboardHeight
    );
    this.installInterval = null;
    this.installCount = 0;
  }

  touchDownTextField = ({ reactTag }) => {
    if (reactTag === findNodeHandle(this.inputRef) && this.props.touchOnFocus) {
      this.props.touchOnFocus()
    }
  };

  onChangeKeyboardHeight = ({ reactTag, height }) => {
    if (reactTag === findNodeHandle(this.inputRef) && this.props.onChangeKeyboardHeight) {
      this.props.onChangeKeyboardHeight(height)
    }
  };

  textInputRef = () => this.inputRef;

  componentWillReceiveProps (nextProps) {
    if (nextProps.keyboardHeight !== this.props.keyboardHeight) {
      setHeight(findNodeHandle(this.inputRef), nextProps.keyboardHeight);

    }
  }

  // successCallback = () => {
  //   clearInterval(this.installInterval);
  // }
  //
  // errorCallback = () => {
  //   if (this.installCount > 5) {
  //     clearInterval(this.installInterval);
  //   }
  //   this.installCount++
  // }

  errorCallback = (err) => {
    Alert.alert(
      'error',
      `${err}`
    )
  };

  componentDidMount () {
    // this.installInterval = setInterval(() => {
    //   installKeyboard(this.props.customKeyboardType, findNodeHandle(this.inputRef), 0, this.successCallback, this.errorCallback)
    // }, 10);
    installKeyboard(this.props.customKeyboardType, findNodeHandle(this.inputRef), 0, this.props.onSuccessInstall, this.errorCallback)
  }

  componentWillUnmount () {
    uninstall(findNodeHandle(this.inputRef));
    this.subscription.remove();
  }

  render () {
    const { ...otherProps } = this.props;
    return (
      <TextInput
        ref={ref => this.inputRef = ref}
        autoCorrect={false}
        {...otherProps}
      />
    );
  }
}
