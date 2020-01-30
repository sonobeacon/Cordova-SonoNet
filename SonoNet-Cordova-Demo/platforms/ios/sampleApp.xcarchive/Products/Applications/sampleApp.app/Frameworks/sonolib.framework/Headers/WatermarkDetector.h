//
//  WatermarkDetector.h
//  Fraunhofer
//
//
//

#import <Foundation/Foundation.h>

@class WatermarkMessage;

typedef void(^WatermarkDetectionBlock)(WatermarkMessage *msg, double confidenceValue);

@interface WatermarkDetector : NSObject

@property (nonatomic, copy) WatermarkDetectionBlock detectionBlock;
@property (nonatomic, assign, getter = isDetecting) BOOL detecting;
@property (nonatomic, assign) NSDictionary* settings;

- (instancetype)initWithPreferredMicro:(NSInteger)microIdx;

- (void)startDetection;
- (void)endDetection;
- (void)setDetectionBlock:(WatermarkDetectionBlock)detectionBlock;
- (void)enableHFMode;
- (void)disableHFMode;
- (void)toggleMode;
- (bool)isHFModeEnabled;
- (void)setupDetector1;
- (void)setupDetector2;
- (void)printMemoryUsage;

@end
