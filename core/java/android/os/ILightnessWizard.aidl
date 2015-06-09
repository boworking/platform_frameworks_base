/**
 * Create by Zhou Jinpei, 2015-04-23
 */
package android.os;

interface ILightnessWizard
{
    boolean isRunning();
    void onSwitch(int flags);
}