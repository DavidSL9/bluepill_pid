#ifndef _VELOCITY_PROFILE_
#define _VELOCITY_PROFILE_

#define TS	0.001

#include <Arduino.h>

enum ProfileState{STOPPED, RUNNING};

class Profile
{
private:
	long _despos;
	long _desvel;
	double accel;
	double period;
	double alpha;
	double theta[2];
	double omega[2];
	int k, T;
	ProfileState state;
public:
	Profile(void);
	~Profile(void);
	void setStep(long);
	void execute(void);
	void startMotion(long);
	void setPosition(long);
	void setVelocity(long);
	float position(void) const;
};

#endif//_VELOCITY_PROFILE_
