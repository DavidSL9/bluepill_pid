#include "profile.h"

Profile::Profile(void)
{
	k = 0;
	theta[0] = 0.0;
	omega[0] = 0.0;
}

Profile::~Profile(void)
{
}

void Profile::setStep(long step)
{
	theta[0] = step;
}

void Profile::execute(void)
{
	if (state == STOPPED)
		return;
	omega[1] = omega[0];
	theta[1] = theta[0];
	float t = k * TS;
	alpha = -2.0 * accel * t / period + accel;
	omega[0] = omega[1] + alpha * TS;
	theta[0] = theta[1] + omega[0] * TS;
	k = k + 1;
	if (k > T)
		state = STOPPED;
}

void Profile::startMotion(long curpos)
{
	theta[0] = (double)curpos;
	period = 1.5 * (abs(_despos) / (double)_desvel);
	T = int(period / TS);
	float sgn = _despos >= 0.0? 1.0 : -1.0;
	accel  = 4000.0 * sgn * _desvel /(double)T;
	k = 0;
	state = RUNNING;
}

void Profile::setPosition(long pos)
{
	_despos = pos;
}

void Profile::setVelocity(long vel)
{
	_desvel = vel;
}

float Profile::position(void) const
{
	return theta[0];
}

