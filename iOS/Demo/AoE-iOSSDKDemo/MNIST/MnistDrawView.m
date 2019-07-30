//
//  MnistDrawView.m
//  AoEOpenSDKDemo
//
//  Created by dingchao on 2019/7/8.
//

#import "MnistDrawView.h"

@interface MnistDrawView ()
//保存之前触摸接触的点
@property (nonatomic, strong) NSMutableArray *lines;
//临时保存本次触摸所有接触的点
@property (nonatomic, strong) NSMutableArray *pointsTemp;

@property (nonatomic, assign) CGAffineTransform invTransform;
@property (nonatomic, assign) CGAffineTransform mTransform;

@end

@implementation MnistDrawView

- (instancetype)initWithFrame:(CGRect)frame {
    self = [super initWithFrame:frame];
    if (self) {
        [self setup];
    }
    return self;
}

- (void)setup {
    self.lines = [NSMutableArray arrayWithCapacity:0];
    self.pointsTemp = [NSMutableArray arrayWithCapacity:0];
}

- (void)clear {
    [self.lines removeAllObjects];
    [self.pointsTemp removeAllObjects];
    [self setNeedsDisplay];
}

/*
 // Only override drawRect: if you perform custom drawing.
 // An empty implementation adversely affects performance during animation.
 */
- (void)drawRect:(CGRect)rect {
    //使用UIKit提供的函数获取当前操作的context
    CGContextRef context = UIGraphicsGetCurrentContext();
    //设置线条帽样式
    CGContextSetLineCap(context, kCGLineCapRound);
    //设置线条连接处的样式
    CGContextSetLineJoin(context, kCGLineJoinRound);
    //设置线条粗细
    CGContextSetLineWidth(context, 5.f);
    //设置颜色
    CGContextSetRGBStrokeColor(context, 0, 0, 0, 1);
    CGContextSetAllowsAntialiasing(context, true);
    CGContextSetShouldSmoothFonts(context, true);
    
    for (NSArray *arr in self.lines) {
        [self renderLines:arr context:context];
    }
    
    [self renderLines:self.pointsTemp context:context];
}

- (void)renderLines:(NSArray *)line context:(CGContextRef)context {
    //重新开始一个起始路径，绘制当前触摸所有的点
    CGContextBeginPath(context);
    for (int i = 0; i < line.count; i++) {
        CGPoint point = CGPointFromString(line[i]);
        if (i == 0) {
            //起始点设置为(0,0):注意这是上下文对应区域中的相对坐标，
            CGContextMoveToPoint(context, point.x, point.y);
        }else{
            //设置下一个坐标点
            CGContextAddLineToPoint(context, point.x, point.y);
        }
    }
    //连接上面定义的坐标点
    CGContextStrokePath(context);
}

- (void)touchesBegan:(NSSet<UITouch *> *)touches withEvent:(UIEvent *)event {
    UITouch *touch = [touches anyObject];
    CGPoint point = [touch locationInView:self];
//    point = CGPointApplyAffineTransform(point, self.invTransform);
    //保存数据
    [self.pointsTemp addObject:NSStringFromCGPoint(point)];
    //通知View需要重新渲染，会触发drawRect:方法的调用
    [self setNeedsDisplay];
}

- (void)touchesMoved:(NSSet *)touches withEvent:(UIEvent *)event{
    UITouch *touch = [touches anyObject];
    CGPoint point = [touch locationInView:self];
//    point = CGPointApplyAffineTransform(point, self.invTransform);
    //保存数据
    [self.pointsTemp addObject:NSStringFromCGPoint(point)];
    //通知View需要重新渲染，会触发drawRect:方法的调用
    [self setNeedsDisplay];
}

- (void)touchesEnded:(NSSet *)touches withEvent:(UIEvent *)event{
    //保存本次触摸所有的点
    [self.lines addObject:self.pointsTemp];
    //清空以便下次触摸时保存数据
    self.pointsTemp = [NSMutableArray array];
}

@end
