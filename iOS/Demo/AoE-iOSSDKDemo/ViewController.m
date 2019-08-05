//
//  ViewController.m
//  AoE
//
//  Created by dingchao on 2019/3/13.
//

#import "ViewController.h"
#import "MnistDemoViewController.h"
#import "SqueezeDemoViewController.h"
#import "AoEDemoMenuViewCell.h"

@interface ViewController ()
<
UITableViewDelegate,
UITableViewDataSource
>

@property (nonatomic, strong) UITableView *mainTableView;
@end
static NSString *const kAoEMenuCellIdentify = @"kAoEMenuCellIdentify";
@implementation ViewController

- (NSArray *)mainTableArray {
    return @[@{@"text":@"MNIST",
               @"image":@"mnist.jpg",
               @"subtext":@"基于TensorFlow Lite实现手写数字识别，其中数字的范围从 0 到 9 。",
               @"action":^{
                   MnistDemoViewController *viewController = [[MnistDemoViewController alloc] init];
                   viewController.title = @"Mnist手写识别";
                   [self.navigationController pushViewController:viewController animated:YES];
               }},
             @{@"text":@"SQUEEZE",
               @"image":@"squeeze.jpg",
               @"subtext":@"NCNN 官方提供的 SqueezeNet 物体图像识别示例。",
               @"action":^{
                   SqueezeDemoViewController *viewController = [[SqueezeDemoViewController alloc] init];
                   viewController.title = @"Squeeze分类";
                   [self.navigationController pushViewController:viewController animated:YES];
               }}
                 ];
}
- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view, typically from a nib.
    self.view.backgroundColor = [UIColor colorWithWhite:242/255.f alpha:    1.f];
    
    self.mainTableView = [[UITableView alloc] initWithFrame:self.view.bounds style:UITableViewStylePlain];
    [self.mainTableView registerClass:[AoEDemoMenuViewCell class] forCellReuseIdentifier:kAoEMenuCellIdentify];
    self.mainTableView.separatorStyle = UITableViewCellSeparatorStyleNone;
    self.mainTableView.backgroundColor = [UIColor clearColor];
    self.mainTableView.delegate   = self;
    self.mainTableView.dataSource = self;
    self.title = @"AoE";
    self.navigationController.navigationBar.titleTextAttributes = @{NSForegroundColorAttributeName:[UIColor whiteColor]};
    [self.view addSubview:self.mainTableView];
}

#pragma mark - UITableViewDelegate
- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return [self mainTableArray].count;
}

- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath {
    return 236.0f;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    UIImage *image = [UIImage imageNamed:[self mainTableArray][indexPath.row][@"image"]];
    AoEDemoMenuViewCell *mainCell = [tableView dequeueReusableCellWithIdentifier:kAoEMenuCellIdentify];
    mainCell.selectionStyle = UITableViewCellSelectionStyleNone;
    [mainCell cellTitle:[self mainTableArray][indexPath.row][@"text"]
               subtitle:[self mainTableArray][indexPath.row][@"subtext"]
                  image:image];
    return mainCell;
}

- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath {
    [tableView deselectRowAtIndexPath:indexPath animated:NO];
    void(^action)(void) = [self mainTableArray][indexPath.row][@"action"];
    action();
}
@end
