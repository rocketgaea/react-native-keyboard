import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { findNodeHandle, NativeEventEmitter, NativeModules, TextInput } from 'react-native';

const { installKeyboard, uninstall } = NativeModules.RNAllmaxKeyboard;

const keyboardManagerEmitter = new NativeEventEmitter(NativeModules.RNAllmaxKeyboardEventEmitter);

export default class TextField extends Component {

  static propTypes = {
    ...TextInput.propTypes,
    customKeyboardType: PropTypes.string.isRequired,
    mountInterval: PropTypes.number,
    touchOnFocus: PropTypes.func,
    onSuccessInstall: PropTypes.func,
  };

  static defaultProps = {
    mountInterval: 40,
    touchOnFocus: () => {},
    onSuccessInstall: () => {},
  };

  constructor (props) {
    super(props);
    this.subscription = keyboardManagerEmitter.addListener(
      'TouchDownTextField',
      this.touchDownTextField
    );
  }

  touchDownTextField = ({ reactTag }) => {
    if (reactTag === findNodeHandle(this.inputRef) && this.props.touchOnFocus) {
      this.props.touchOnFocus()
    }
  };

  textInputRef = () => this.inputRef;

  componentWillReceiveProps (nextProps) {
    if (nextProps.keyboardHeight !== this.props.keyboardHeight) {
      installKeyboard(this.props.customKeyboardType, findNodeHandle(this.inputRef), nextProps.keyboardHeight, () => {});
    }
  }

  componentDidMount () {
    setTimeout(() => installKeyboard(
      this.props.customKeyboardType, findNodeHandle(this.inputRef), this.props.keyboardHeight, this.props.onSuccessInstall
    ), this.props.mountInterval);
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