require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name                = "RNAllmaxKeyboard"
  s.version             = package['version']
  s.summary             = "1"
  s.homepage            = "https://github.com/rocketgaea/react-native-keyboard/new/master"
  s.license             = "MIT"
  s.author              = package['author']
  s.source              = { :git => 'https://github.com/rocketgaea/react-native-keyboard.git', :tag => "v#{s.version}" }
  s.default_subspec     = 'Core'
  s.requires_arc        = true
  s.platform            = :ios, "7.0"

  s.dependency 'React'

  s.subspec 'Core' do |ss|
    ss.source_files     = "RNAllmaxKeyboard/*.{h,m}"
  end

end
