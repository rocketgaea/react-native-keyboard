//
//  RNAllmaxKeyboardEventEmitter.m
//  RNAllmaxKeyboard
//
//  Created by Oleg on 29.08.17.
//  Copyright © 2017 Facebook. All rights reserved.
//

#import "RNAllmaxKeyboardEventEmitter.h"

@implementation RNAllmaxKeyboardEventEmitter

@synthesize bridge = _bridge;

- (instancetype)init
{
    self = [super init];
    if (self) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(touchDownTextFieldWithNotification:) name:@"RNAllMaxKeyboardTouchDownTextField" object:nil];
    }
    return self;
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE(RNAllmaxKeyboardEventEmitter)

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"TouchDownTextField"];
}

- (void)touchDownTextFieldWithNotification: (NSNotification *) notification {
    NSLog(@"%@", notification);
    [self sendEventWithName:@"TouchDownTextField" body:notification.userInfo];
}


@end
