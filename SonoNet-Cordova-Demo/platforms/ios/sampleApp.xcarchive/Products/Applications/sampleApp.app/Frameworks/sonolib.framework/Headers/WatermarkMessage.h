//
//  WatermarkMessage.h
//  WatermarkDetectionFramework
//
//  Created by MSF on 17.01.12.
//  Copyright (c) 2012 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface WatermarkMessage : NSObject
@property (strong, nonatomic) NSString* message;
@property double totalScore;
@property int length;

-(id) initWithASCIIBits: (char*) messageChars ofLength: (int) messageLength;
-(NSString*) toString;
-(NSString*) toHex;


@end
