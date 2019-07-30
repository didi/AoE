//
//  AoEGraphicUtil.m
//  AoE
//
//  Created by dingchao on 2019/7/9.
//

#import "AoEGraphicUtil.h"
#import <CoreMedia/CoreMedia.h>
#import <Accelerate/Accelerate.h>

@implementation AoEGraphicUtil
+ (CVImageBufferRef)resizeImageBuffer:(CVImageBufferRef)imageBuffer react:(CGSize)size {
    return [self scaleImageBuffer:imageBuffer
                         fromRect:CGRectMake(0, 0, CVPixelBufferGetWidth(imageBuffer), CVPixelBufferGetHeight(imageBuffer))
                       targetSize:size];
}

+ (CVImageBufferRef)cropImageBuffer:(CVImageBufferRef)imageBuffer react:(CGRect)react {
    return [self scaleImageBuffer:imageBuffer fromRect:react targetSize:react.size];
}

+ (CVImageBufferRef)scaleImageBuffer:(CVPixelBufferRef)imageBuffer
                            fromRect:(CGRect)fromRect
                          targetSize:(CGSize)targetSize {
    CVImageBufferRef outerBuffer = NULL;
    size_t cropX0    = fromRect.origin.x;
    size_t cropY0    = fromRect.origin.y;
    size_t cropHeight= fromRect.size.height;
    size_t cropWidth = fromRect.size.width;
    size_t outWidth  = targetSize.width;
    size_t outHeight = targetSize.height;
    
    size_t bufferheight= CVPixelBufferGetHeight(imageBuffer);
    size_t bufferwidth = CVPixelBufferGetWidth(imageBuffer);
    
    if (cropY0 + cropHeight > bufferheight ||
        cropX0 + cropWidth > bufferwidth) {
        return nil;
    }
    
    CVPixelBufferLockBaseAddress(imageBuffer,0);
    uint8_t *baseAddress = (unsigned char *)CVPixelBufferGetBaseAddress(imageBuffer);
    OSType pixelFormatType = CVPixelBufferGetPixelFormatType(imageBuffer);
    size_t bytesPerPixel = [self getBytesPerPixelFromPixelFormatType:pixelFormatType];
    void *scaledData = [self vImageScaleImageBuffer:imageBuffer baseaddress:baseAddress fromRect:fromRect targetSize:targetSize];
    
    CVReturn result = CVPixelBufferCreateWithBytes(NULL,
                                                   outWidth,
                                                   outHeight,
                                                   pixelFormatType,
                                                   scaledData,
                                                   bytesPerPixel * outWidth,
                                                   &freePixelBufferDataAfterRelease,
                                                   NULL,
                                                   NULL,
                                                   &outerBuffer);
    
    CVPixelBufferUnlockBaseAddress(imageBuffer,0);
    if (result != kCVReturnSuccess) {
        NSLog(@"Error: could not create new pixel buffer");
        free(scaledData);
    }
    return outerBuffer;
}

+ (void *)vImageScaleImageBuffer:(CVPixelBufferRef)imageBuffer
                     baseaddress:(void *)baseAddress
                        fromRect:(CGRect)fromRect
                      targetSize:(CGSize)targetSize {
    
    size_t cropX0    = fromRect.origin.x;
    size_t cropY0    = fromRect.origin.y;
    size_t cropHeight= fromRect.size.height;
    size_t cropWidth = fromRect.size.width;
    size_t outWidth  = targetSize.width;
    size_t outHeight = targetSize.height;
    
    size_t bytesPerRow = CVPixelBufferGetBytesPerRow(imageBuffer);
    OSType pixelFormatType = CVPixelBufferGetPixelFormatType(imageBuffer);
    size_t bytesPerPixel = [self getBytesPerPixelFromPixelFormatType:pixelFormatType];
    vImage_Buffer inBuff;
    inBuff.height = cropHeight;
    inBuff.width = cropWidth;
    inBuff.rowBytes = bytesPerRow;
    
    int startpos = (int)cropY0 * (int)bytesPerRow + (int)bytesPerPixel * (int)cropX0;
    inBuff.data = baseAddress + startpos;
    
    unsigned char *outImg = (unsigned char*)malloc(bytesPerPixel * outWidth * outHeight);
    vImage_Buffer outBuff = {outImg, outHeight, outWidth, bytesPerPixel * outWidth};
    
    vImage_Error err = vImageScale_ARGB8888(&inBuff, &outBuff, NULL, 0);
    if (err != kvImageNoError) {
        NSLog(@" error %ld", err);
        free(outImg);
        return NULL;
    }
    return outBuff.data;
}

+ (CVPixelBufferRef)pixelBufferFromImage:(UIImage *)image {
    
    NSDictionary *options = @{
                              (NSString*)kCVPixelBufferCGImageCompatibilityKey : @YES,
                              (NSString*)kCVPixelBufferCGBitmapContextCompatibilityKey : @YES,
                              (NSString*)kCVPixelBufferIOSurfacePropertiesKey: @{}
                              };
    CVPixelBufferRef pxbuffer = NULL;
    
    CGFloat frameWidth = CGImageGetWidth(image.CGImage);
    CGFloat frameHeight = CGImageGetHeight(image.CGImage);
    
    CVReturn status = CVPixelBufferCreate(kCFAllocatorDefault,
                                          frameWidth,
                                          frameHeight,
                                          kCVPixelFormatType_32ARGB,
                                          (__bridge CFDictionaryRef) options,
                                          &pxbuffer);
    
    NSParameterAssert(status == kCVReturnSuccess && pxbuffer != NULL);
    
    CVPixelBufferLockBaseAddress(pxbuffer, 0);
    void *pxdata = CVPixelBufferGetBaseAddress(pxbuffer);
    NSParameterAssert(pxdata != NULL);
    
    CGColorSpaceRef rgbColorSpace = CGColorSpaceCreateDeviceRGB();
    
    CGContextRef context = CGBitmapContextCreate(pxdata,
                                                 frameWidth,
                                                 frameHeight,
                                                 8,
                                                 CVPixelBufferGetBytesPerRow(pxbuffer),
                                                 rgbColorSpace,
                                                 CGImageGetBitmapInfo(image.CGImage));
    NSParameterAssert(context);
    CGContextConcatCTM(context, CGAffineTransformIdentity);
    CGContextDrawImage(context, CGRectMake(0,
                                           0,
                                           frameWidth,
                                           frameHeight),
                       image.CGImage);
    CGColorSpaceRelease(rgbColorSpace);
    CGContextRelease(context);
    
    CVPixelBufferUnlockBaseAddress(pxbuffer, 0);
    
    return pxbuffer;
}

+ (CGContextRef)newBitmapRGBA8ContextWithWidth:(size_t)width height:(size_t)height Info:(CGBitmapInfo)info {
    CGContextRef context = NULL;
    CGColorSpaceRef colorSpace;
    uint32_t *bitmapData;
    size_t bitsPerPixel = 32;
    size_t bitsPerComponent = 8;
    size_t bytesPerPixel = bitsPerPixel / bitsPerComponent;
    
    size_t bytesPerRow = width * bytesPerPixel;
    size_t bufferLength = bytesPerRow * height;
    
    colorSpace = CGColorSpaceCreateDeviceRGB();
    
    if(!colorSpace) {
        NSLog(@"Error allocating color space RGB\n");
        return NULL;
    }
    
    // Allocate memory for image data
    bitmapData = (uint32_t *)malloc(bufferLength);
    
    if(!bitmapData) {
        NSLog(@"Error allocating memory for bitmap\n");
        CGColorSpaceRelease(colorSpace);
        return NULL;
    }
    
    //Create bitmap context
    
    context = CGBitmapContextCreate(bitmapData,
                                    width,
                                    height,
                                    bitsPerComponent,
                                    bytesPerRow,
                                    colorSpace,
                                    info);  // RGBA
    //    bitmapInfoWithPixelFormatType(kCVPixelFormatType_32BGRA);
    if(!context) {
        free(bitmapData);
        NSLog(@"Bitmap context not created");
    }
    
    CGColorSpaceRelease(colorSpace);
    
    return context;
}

+ (CGContextRef)newBitmapRGBA8ContextFromCGImage:(CGImageRef)image {
   return [self newBitmapRGBA8ContextWithWidth:CGImageGetWidth(image)
                                        height:CGImageGetHeight(image)
                                          Info:CGImageGetBitmapInfo(image)];
}

+ (CGContextRef) newBitmapRGBA8ContextFromPixelBuffer:(CVImageBufferRef)buffer {
    OSType pixelFormatType = CVPixelBufferGetPixelFormatType(buffer);
    return [self newBitmapRGBA8ContextWithWidth:CVPixelBufferGetWidth(buffer)
                                         height:CVPixelBufferGetHeight(buffer)
                                           Info:[self bitmapInfoWithPixelFormatType:pixelFormatType]];
}


+ (unsigned char *)RGBA8BitmapFromBuffer:(CVPixelBufferRef)pixelBuffer  {
    size_t height = CVPixelBufferGetHeight(pixelBuffer);
    CVPixelBufferLockBaseAddress(pixelBuffer, 0);
    // Get a pointer to the data
    unsigned char *bitmapData = CVPixelBufferGetBaseAddress(pixelBuffer);
    size_t bytesPerRow = CVPixelBufferGetBytesPerRow(pixelBuffer);
    size_t bufferLength = bytesPerRow * height;
    unsigned char *newBitmap = NULL;
    if(bitmapData) {
        newBitmap = (unsigned char *)malloc(sizeof(unsigned char) * bytesPerRow * height);
        
        if(newBitmap) {
            for (int i = 0; i < bufferLength / 4; i++) {
                NSUInteger byteIndex = i * [self getBytesPerPixelFromPixelFormatType:kCVPixelFormatType_32BGRA];
                NSUInteger newByteIndex = i * [self getBytesPerPixelFromPixelFormatType:kCVPixelFormatType_32RGBA];
                // Set RGB To New RawData
                newBitmap[newByteIndex + 0] = bitmapData[byteIndex + 1];   // R
                newBitmap[newByteIndex + 1] = bitmapData[byteIndex + 2];  // G
                newBitmap[newByteIndex + 2] = bitmapData[byteIndex + 3];    // B
                newBitmap[newByteIndex + 3] = bitmapData[byteIndex + 0];    // A
            }
        }
        
        free(bitmapData);
        
    } else {
        printf("bitmapData null");
        NSLog(@"Error getting bitmap pixel data\n");
    }
    CVPixelBufferUnlockBaseAddress(pixelBuffer, 0);
    
    return newBitmap;
}

+ (UIImage *)RGBA8ImageFrombitmap:(unsigned char *)buffer
                             size:(CGSize)size {
    
    size_t bufferLength = size.width * size.height * 4;
    CGDataProviderRef provider = CGDataProviderCreateWithData(NULL, buffer, bufferLength, NULL);
    size_t bitsPerComponent = 8;
    size_t bitsPerPixel = 32;
    size_t bytesPerRow = 4 * size.width;
    
    CGColorSpaceRef colorSpaceRef = CGColorSpaceCreateDeviceRGB();
    if(colorSpaceRef == NULL) {
        NSLog(@"Error allocating color space");
        CGDataProviderRelease(provider);
        return nil;
    }
    
    CGBitmapInfo bitmapInfo = kCGBitmapByteOrderDefault;
    CGColorRenderingIntent renderingIntent = kCGImageAlphaNoneSkipFirst | kCGBitmapByteOrder32Host;
    
    CGImageRef iref = CGImageCreate(size.width,
                                    size.height,
                                    bitsPerComponent,
                                    bitsPerPixel,
                                    bytesPerRow,
                                    colorSpaceRef,
                                    bitmapInfo,
                                    provider,    // data provider
                                    NULL,        // decode
                                    YES,            // should interpolate
                                    renderingIntent);
    if(iref == NULL) {
        CGDataProviderRelease(provider);
        CGColorSpaceRelease(colorSpaceRef);
        return nil;
    }
    CGContextRef context = [self newBitmapRGBA8ContextFromCGImage:iref];
    UIImage *image = nil;
    if(context) {
        CGContextDrawImage(context, CGRectMake(0.0f, 0.0f, size.width, size.height), iref);
        CGImageRef imageRef = CGBitmapContextCreateImage(context);
        // Support both iPad 3.2 and iPhone 4 Retina displays with the correct scale
        if([UIImage respondsToSelector:@selector(imageWithCGImage:scale:orientation:)]) {
            float scale = [[UIScreen mainScreen] scale];
            image = [UIImage imageWithCGImage:imageRef scale:scale orientation:UIImageOrientationUp];
        } else {
            image = [UIImage imageWithCGImage:imageRef];
        }
        CGImageRelease(imageRef);
        CGContextRelease(context);
    }
    
    CGColorSpaceRelease(colorSpaceRef);
    CGImageRelease(iref);
    CGDataProviderRelease(provider);
    return image;
}

+ (uint32_t)bitmapInfoWithPixelFormatType:(OSType)inputPixelFormat {
    /*
     typedef CF_ENUM(uint32_t, CGImageAlphaInfo) {
     kCGImageAlphaNone,                For example, RGB.
     kCGImageAlphaPremultipliedLast,   For example, premultiplied RGBA
     kCGImageAlphaPremultipliedFirst,  For example, premultiplied ARGB
     kCGImageAlphaLast,                For example, non-premultiplied RGBA
     kCGImageAlphaFirst,               For example, non-premultiplied ARGB
     kCGImageAlphaNoneSkipLast,        For example, RBGX.
     kCGImageAlphaNoneSkipFirst,       For example, XRGB.
     kCGImageAlphaOnly                 No color data, alpha data only
     };
     */
    if (inputPixelFormat == kCVPixelFormatType_32BGRA) {
        //uint32_t bitmapInfo = kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Host;
        //此格式也可以
        return kCGImageAlphaNoneSkipFirst | kCGBitmapByteOrder32Host;
    }else if (inputPixelFormat == kCVPixelFormatType_32ARGB){
        //此格式也可以
        //uint32_t bitmapInfo = kCGImageAlphaNoneSkipFirst | kCGBitmapByteOrder32Big;
        return kCGImageAlphaPremultipliedFirst | kCGBitmapByteOrder32Big;
    }
    NSLog(@"不支持此格式");
    return 0;
}

+ (size_t)getBytesPerPixelFromPixelFormatType:(OSType)type {
    size_t pixelFormat = 4;
    switch (type) {
        case kCVPixelFormatType_24RGB :
            pixelFormat = 3;
            break;
        case kCVPixelFormatType_24BGR :
            pixelFormat = 3;
            break;
        default:
            pixelFormat = 4;
            break;
    }
    return pixelFormat;
}

void freePixelBufferDataAfterRelease(void *releaseRefCon, const void *baseAddress) {
    free((void *)baseAddress);
}
@end
