//
//  SqueezeMNNDemoViewController.m
//  AoEOpenSDKDemo
//
//  Created by dingchao on 2019/9/10.
//

#import "SqueezeMNNDemoViewController.h"
#import <CoreServices/CoreServices.h>
#import <AoE/AoE.h>
@interface SqueezeMNNDemoViewController ()
<UINavigationControllerDelegate,UIImagePickerControllerDelegate>
@property (nonatomic, strong) AoEClient *client;
@property (nonatomic, strong) UIImageView *drawView;
@property (nonatomic, strong) UILabel *resultLabel;
@end

@implementation SqueezeMNNDemoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.view.backgroundColor = [UIColor colorWithWhite:242/255.f alpha:    1.f];
    [self setupView];
    AoEClientOption *clientOption = [AoEClientOption new];
    clientOption.interpreterClassName = @"ABSqueezeNet2Interceptor";
//    clientOption.modelOptionLoaderClassName = @"ABSqueezeModelManager";
    
    NSString *dir = [[[NSBundle mainBundle] resourcePath] stringByAppendingPathComponent:@"AoEBiz-squeeze_mnn.bundle/squeeze_mnn/recognize-mnn"];
    AoEClient *client = [[AoEClient alloc] initWithClientOption:clientOption modelDir:dir subDir:nil];
    __weak typeof(self) weak_self = self;
    [client setupModel:^(AoEClientStatusCode statusCode) {
        __strong typeof(weak_self) strong_self = weak_self;
        strong_self.resultLabel.text = [@"status message is " stringByAppendingString:@(statusCode).stringValue];
    }];
    self.client = client;
}

- (void)setupView {
    CGFloat screenWidth = [UIScreen mainScreen].bounds.size.width;
    CGFloat screenHeight = [UIScreen mainScreen].bounds.size.height;
    self.drawView = [[UIImageView alloc] initWithFrame:CGRectMake(16, 80, screenWidth - 32, screenWidth - 32)];
    self.drawView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:self.drawView];
    
    UIView *barView = [[UIView alloc] initWithFrame:CGRectMake(0, screenHeight - 60, screenWidth, 60)];
    barView.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:barView];
    
    UIButton *clearButton = [UIButton buttonWithType:UIButtonTypeCustom];
    clearButton.frame = CGRectMake(16, 10, screenWidth - 32, 40);
    [clearButton setTitleColor:[UIColor whiteColor] forState:UIControlStateNormal];
    [clearButton setBackgroundColor:[UIColor colorWithRed:68/255.f green:137/255.f blue:247/255.f alpha:1.f]];
    [clearButton setTitle:@"选图" forState:UIControlStateNormal];
    [clearButton addTarget:self action:@selector(execute:) forControlEvents:UIControlEventTouchUpInside];
    [barView addSubview:clearButton];
    CGRect restultRect = CGRectMake(16,
                                    self.drawView.frame.size.height + self.drawView.frame.origin.y + 60,
                                    (screenWidth - 32) ,
                                    40);
    self.resultLabel = [[UILabel alloc] initWithFrame:restultRect];
    self.resultLabel.preferredMaxLayoutWidth = (screenWidth - 32);
    self.resultLabel.numberOfLines = 0;
    self.resultLabel.backgroundColor = [UIColor whiteColor];
    [self.view addSubview:self.resultLabel];
}

- (IBAction)execute:(id)sender {
    if ([UIImagePickerController isSourceTypeAvailable:UIImagePickerControllerSourceTypeSavedPhotosAlbum]) {
        UIImagePickerController *picker = [[UIImagePickerController alloc] init];
        picker.sourceType = UIImagePickerControllerSourceTypeSavedPhotosAlbum;
        picker.mediaTypes = @[(NSString *)kUTTypeImage];
        picker.delegate = self;
        // model出控制器
        [self presentViewController:picker animated:YES completion:NULL];
    }
}

- (void)imagePickerController:(UIImagePickerController *)picker
didFinishPickingMediaWithInfo:(NSDictionary<UIImagePickerControllerInfoKey,id> *)info {
    self.drawView.image = info[UIImagePickerControllerOriginalImage];
    NSString *result = (NSString *)[self.client process:((id<AoEInputModelProtocol>)self.drawView.image)];
    self.resultLabel.text = result;
    [self dismissViewControllerAnimated:YES completion:nil];
    NSLog(@"%@", info);
}

// 取消图片选择调用此方法
- (void)imagePickerControllerDidCancel:(UIImagePickerController *)picker {
    [self dismissViewControllerAnimated:YES completion:nil];
}

@end
