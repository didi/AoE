//
//  MnistDemoViewController.m
//  AoEOpenSDKDemo
//
//  Created by dingchao on 2019/7/3.
//

#import "MnistDemoViewController.h"
#import "MnistDrawView.h"
#import <AoE/AoE.h>

@interface MnistDemoViewController ()

@property (nonatomic, strong) AoEClient *client;
@property (nonatomic, strong) MnistDrawView *drawView;
@property (nonatomic, strong) UILabel *resultLabel;
@end

@implementation MnistDemoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    [self setupView];
    self.view.backgroundColor = [UIColor colorWithWhite:242/255.f alpha:    1.f];
    // AoEClient 初始化
    AoEClientOption *clientOption = [AoEClientOption new];
    clientOption.interpreterClassName = @"ABMnistInterceptor";
    clientOption.appKey = @"FKuABwKYV18bwT";
    clientOption.lat = @"39.92";
    clientOption.lng = @"116.46";
    clientOption.appId = 164;
    NSString *dir = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"AoEBiz-mnist.bundle/mnist"];
    AoEClient *client = [[AoEClient alloc] initWithClientOption:clientOption modelDir:dir subDir:nil];
    __weak typeof(self) weak_self = self;
    [client setupModel:^(AoEClientStatusCode statusCode) {
        __strong typeof(weak_self) strong_self = weak_self;
        strong_self.resultLabel.text = [@"status message is " stringByAppendingString:@(statusCode).stringValue];
    }];
    self.client = client;
    
}

-(void)dealloc {
    [self.client close];
}

- (void)setupView {
    CGFloat screenWidth = [UIScreen mainScreen].bounds.size.width;
    CGFloat screenHeight = [UIScreen mainScreen].bounds.size.height;
    self.drawView = [[MnistDrawView alloc] initWithFrame:CGRectMake(16, 80, screenWidth - 32, screenWidth - 32)];
    self.drawView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:self.drawView];
    
    UIView *barView = [[UIView alloc] initWithFrame:CGRectMake(0, screenHeight - 60, screenWidth, 60)];
    barView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:barView];
    
    UIButton *regnizeButton = [UIButton buttonWithType:UIButtonTypeCustom];
    regnizeButton.frame = CGRectMake(16, 10, (screenWidth - 32 - 10) / 2, 40);
    [regnizeButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [regnizeButton setTitle:@"识别" forState:UIControlStateNormal];
    [regnizeButton addTarget:self action:@selector(execute:) forControlEvents:UIControlEventTouchUpInside];
    [regnizeButton setBackgroundColor:[UIColor colorWithRed:68/255.f green:137/255.f blue:247/255.f alpha:1.f]];
    [barView addSubview:regnizeButton];
    
    UIButton *clearButton = [UIButton buttonWithType:UIButtonTypeCustom];
    
    clearButton.frame = CGRectMake(CGRectGetMaxX(regnizeButton.frame) + 10,
                                   10,
                                   (screenWidth - 32 - 10) / 2,
                                   40);
    [clearButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearButton setTitle:@"清除" forState:UIControlStateNormal];
    [clearButton addTarget:self action:@selector(clear:) forControlEvents:UIControlEventTouchUpInside];
    [clearButton setBackgroundColor:[UIColor colorWithRed:68/255.f green:137/255.f blue:247/255.f alpha:1.f]];
    [barView addSubview:clearButton];
    
   
    CGRect resultRect = CGRectMake(16,
                                   CGRectGetMaxY(self.drawView.frame) + 60,
                                   (screenWidth - 32)  ,
                                   40);
    self.resultLabel = [[UILabel alloc] initWithFrame:resultRect];
//    self.resultLabel.textAlignment = NSTextAlignmentCenter;
    self.resultLabel.numberOfLines = 0;
    self.resultLabel.preferredMaxLayoutWidth = screenWidth - 32;
    self.resultLabel.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:self.resultLabel];
}

- (IBAction)execute:(id)sender {
    
    UIGraphicsBeginImageContext(self.drawView.bounds.size);
    [self.drawView.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    NSNumber *result = (NSNumber *)[self.client process:(id<AoEInputModelProtocol>)image];
    self.resultLabel.text = result.stringValue;
}

- (IBAction)clear:(id)sender {
    [self.drawView clear];
}

@end
