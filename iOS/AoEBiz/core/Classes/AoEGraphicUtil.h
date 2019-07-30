//
//  AoEGraphicUtil.h
//  AoE
//
//  Created by dingchao on 2019/7/9.
//

#import <Foundation/Foundation.h>

#import <UIKit/UIKit.h>
#import <CoreVideo/CoreVideo.h>
#import <CoreGraphics/CoreGraphics.h>

@interface AoEGraphicUtil : NSObject

+ (CVImageBufferRef)resizeImageBuffer:(CVImageBufferRef)imageBuffer react:(CGSize)size;
+ (CVImageBufferRef)cropImageBuffer:(CVImageBufferRef)imageBuffer react:(CGRect)react;


+ (CVPixelBufferRef)pixelBufferFromImage:(UIImage *)image;
+ (unsigned char *)RGBA8BitmapFromBuffer:(CVPixelBufferRef)pixelBuffer;
+ (UIImage *)RGBA8ImageFrombitmap:(unsigned char *)buffer size:(CGSize)size;
@end

