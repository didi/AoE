//
//  AoEDemoMenuViewCell.m
//  AoE-iOSSDKDemo
//
//  Created by dingchao on 2019/3/27.
//

#import "AoEDemoMenuViewCell.h"

@interface AoEDemoMenuViewCell ()
@property (nonatomic, strong) UIImageView *topImageView;
@property (nonatomic, strong) UILabel *title;
@property (nonatomic, strong) UILabel *subTitle;
@property (nonatomic, strong) UIView *backView;
@end

@implementation AoEDemoMenuViewCell

- (void)cellTitle:(NSString *)title
         subtitle:(NSString *)subTile
            image:(UIImage *)image {
//    self.title.text = title;
    self.subTitle.text = subTile;
    self.topImageView.image = image;
}

- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier
{
    if (self = [super initWithStyle:style reuseIdentifier:reuseIdentifier]) {
        [self commonInit];
    }
    return self;
}

- (void)commonInit
{
    [self addSubview:self.backView];
//    [self addSubview:self.title];
    [self addSubview:self.subTitle];
    
    [self addSubview:self.topImageView];
}

-(void)layoutSubviews {
    [super layoutSubviews];
    self.backgroundColor = [UIColor clearColor];
    [self layoutElements];
}

- (void)layoutElements
{
    self.backView.frame = CGRectMake(16, 16, self.frame.size.width - 32, 220);
    self.topImageView.frame = CGRectMake(16, 10, self.frame.size.width - 32 , 172);
//    self.title.frame = CGRectMake(self.topImageView.frame.origin.x + 16,
//                                  self.topImageView.frame.origin.y + self.topImageView.frame.size.height + 8,
//                                  self.topImageView.frame.size.width - 32,
//                                  18);
    self.subTitle.frame = CGRectMake(self.topImageView.frame.origin.x + 16,
                                     self.topImageView.frame.origin.y + self.topImageView.frame.size.height + 8,
                                     self.topImageView.frame.size.width - 32,
                                     32);
}



- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

- (UIView *)backView {
    if (!_backView) {
        _backView = [[UIView alloc] init];
        _backView.backgroundColor = [UIColor whiteColor];
    }
    return _backView;
}

- (UIImageView *)topImageView {
    if (!_topImageView) {
        _topImageView = [[UIImageView alloc] init];
        _topImageView.contentMode = UIViewContentModeScaleAspectFit;
        _topImageView.layer.masksToBounds = YES;
    }
    return _topImageView;
}

- (UILabel *)title {
    if (!_title) {
        _title = [[UILabel alloc] init];
        _title.textColor = [UIColor blackColor];
        _title.font = [UIFont systemFontOfSize:18];
    }
    return _title;
}

- (UILabel *)subTitle {
    if (!_subTitle) {
        _subTitle = [[UILabel alloc] init];
        _subTitle.textColor = [UIColor blackColor];
        _subTitle.font = [UIFont systemFontOfSize:13];
        _subTitle.numberOfLines = 0;
    }
    return _subTitle;
}

@end
