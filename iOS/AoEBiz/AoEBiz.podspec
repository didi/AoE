#
# Any lines starting with a # are optional, but their use is encouraged
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'AoEBiz'
  s.version          = '1.0.0'
  s.summary          = 'AoE Biz pods'
  s.description      = <<-DESC
        this pod is a demo for Biz
                       DESC
  s.homepage         = 'https://github.com/didi/aoe/'
  s.license          = { :type => 'Apache', :file => 'LICENSE' }
  s.author           = { 'dingc' => 'dc328466990@163.com' }  
  s.source           = { :git => "git@github.com:didi/aoe.git.git", :tag => s.version.to_s }

  s.ios.deployment_target = '8.0'
  s.prefix_header_file = false
  s.default_subspec = 'mnist'
  s.prepare_command = "sh scripts/pull_models.sh"

  s.subspec 'Core' do |ss|
    ss.ios.deployment_target = '8.0'
    ss.source_files = "core/Classes/**/*"
    ss.public_header_files = "core/Classes/**/*.h"
    ss.frameworks = 'CoreImage','UIKit','CoreGraphics','Foundation'
    ss.dependency 'AoE'
    ss.resource_bundles = {
      'AoEBiz-Core' => ["core/Assets/**/*"]
    }
  end

  s.subspec 'mnist' do |ss|
    ss.ios.deployment_target = '8.0'
    ss.source_files = "mnist/Classes/**/*"
    ss.public_header_files = "mnist/Classes/**/*.h"
    ss.dependency "#{s.name}/Core"
    ss.dependency "TensorFlowLiteObjC"
    ss.frameworks = 'CoreVideo','UIKit','CoreGraphics','Foundation'
    ss.libraries = ['c++']
    ss.resource_bundles = {
      'AoEBiz-mnist' => ["mnist/Assets/**/*","mnist/Models/**/*"]
    }
  end

  s.subspec 'squeeze' do |ss|
    ss.ios.deployment_target = '8.0'
    ss.source_files = "squeeze/Classes/**/*"
    ss.public_header_files = "squeeze/Classes/**/*.h"
    ss.dependency "#{s.name}/Core"
    # ss.vendored_frameworks = "squeeze/Frameworks/ncnn.framework","squeeze/Frameworks/openmp.framework"
    ss.frameworks = 'CoreVideo','UIKit','CoreGraphics','Foundation'
    ss.libraries = ['c++']
    ss.resource_bundles = {
      'AoEBiz-squeeze' => ["squeeze/Assets/**/*","squeeze/Models/**/*"]
    }
  end

end
