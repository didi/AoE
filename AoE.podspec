#
# Be sure to run `pod lib lint AoE.podspec' to ensure this is a
# valid spec before submitting.
#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'AoE'
  s.version          = '1.0.0'
  s.summary          = 'AoE'

# This description is used to generate tags and improve search results.
#   * Think: What does it do? Why did you write it? What is the focus?
#   * Try to keep it short, snappy and to the point.
#   * Write the description between the DESC delimiters below.
#   * Finally, don't worry about the indent, CocoaPods strips it!

  s.description      = <<-DESC
  AoE is AI on Edage
                       DESC
  s.homepage         = 'https://github.com/didi/aoe/'
  s.license          = { :type => 'Apache', :file => 'LICENSE' }
  s.author           = { 'dingc' => 'dc328466990@163.com' }  
  s.source           = { :git => "git@github.com:didi/aoe.git", :tag => s.version.to_s }

  s.prefix_header_file = false
  s.default_subspec = 'Loader'
  s.platform = :ios
  s.ios.deployment_target = '8.0'
  
  s.subspec 'Core' do |ss|
    ss.source_files = "iOS/#{s.name}/library/Core/Classes/**/*"
    ss.public_header_files = "iOS/#{s.name}/library/Core/Classes/**/*.h"
    ss.frameworks = 'Foundation','UIKit'
    # ss.resource_bundles = {
    #   'iOS-Core' => ["iOS/#{s.name}/library/Core/Assets/**/*"]
    # }
  end
  
  # extension for core
  s.subspec 'Loader' do |ss|
    ss.ios.deployment_target = '8.0'
    ss.source_files = "iOS/#{s.name}/library/Loader/Classes/**/*"
    ss.public_header_files = "iOS/#{s.name}/library/Loader/Classes/**/*.h"
    ss.frameworks = 'CoreGraphics','CoreVideo'
    ss.dependency "#{s.name}/Core"
    ss.dependency 'JSONModel'
    # ss.resource_bundles = {
    #   'iOS-Loader' => ["iOS/#{s.name}/library/Loader/Assets/**/*"]
    # }
  end

  s.subspec 'Logger' do |ss|
    ss.ios.deployment_target = '8.0'
    ss.source_files = "iOS/#{s.name}/library/Logger/Classes/**/*"
    ss.public_header_files = "iOS/#{s.name}/library/Logger/Classes/**/*.h"
    ss.dependency "#{s.name}/Core"
    # ss.resource_bundles = {
    #   'iOS-Logger' => ["iOS/#{s.name}/library/Logger/Assets/**/*"]
    # }
  end

  s.subspec 'Crypto' do |ss|
    ss.ios.deployment_target = '8.0'
    ss.source_files = "iOS/#{s.name}/library/Crypto/Classes/**/*"
    ss.public_header_files = "iOS/#{s.name}/library/Crypto/Classes/**/*.h"
    ss.dependency "#{s.name}/Loader"
  end
  
end
