package project2;

import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;



public class threadz {	
	static int pCount = 0;
	static int dCount = 0;
	static int nCount = 0;
	//static int full = Integer.parseInt(args[1]);
	static int blackboard = 0;
	static int assignment = 0;
	static int folder = 0;
	static int diagnosis = 0;
	static Random rand = new Random();
	public static void main(String[] args) {
		Semaphore sem = new Semaphore(0);
		Semaphore r_release = new Semaphore(0);
		Semaphore sBlackboard = new Semaphore(1);
		Semaphore greenlight = new Semaphore(0, true);
		Semaphore sHallway[] = {new Semaphore(1),new Semaphore(1),new Semaphore(1)};
		Semaphore hGreenlight[] = {new Semaphore(0),new Semaphore(0),new Semaphore(0)};
		Semaphore sOffice[] = {new Semaphore(1),new Semaphore(1),new Semaphore(1)};
		Semaphore oGreenlight[] = {new Semaphore(0),new Semaphore(0),new Semaphore(0)};
		Semaphore n_Call[] = {new Semaphore(0),new Semaphore(0),new Semaphore(0)};
		Semaphore d_Call[] = {new Semaphore(0),new Semaphore(0),new Semaphore(0)};
		Semaphore nd_Call[] = {new Semaphore(0),new Semaphore(0),new Semaphore(0)};
		Thread patients[] = new Thread[Integer.parseInt(args[0])];
		Thread nurses[] = new Thread[Integer.parseInt(args[1])];
		Thread doctors[] = new Thread[Integer.parseInt(args[1])];
		
		System.out.println("Running with " + Integer.toString(Integer.parseInt(args[0])) + " Patients and " + Integer.toString(Integer.parseInt(args[1])) + " Doctors and Nurses");
		
		//starts the patients
		for(int i = 0; i < Integer.parseInt(args[0]); i++){
			patients[i] = new Thread(new Runnable(){
				
				public void run(){
					int ID = 0;
					int dnum = 0;
					ID = pCount;
					++pCount;
					System.out.println("Patient " + Integer.toString(ID) + " enters the reception, waits to be registered.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					sem.release();
					//writes his ID on the blackboard
					try {
						sBlackboard.acquire();
						
						blackboard = ID;
						
						greenlight.acquire();
						
						dnum = assignment;
					} catch (InterruptedException e1) {
						
						e1.printStackTrace();
					} finally {
						sBlackboard.release();
					}
					
					try {
						r_release.acquire();
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					System.out.println("Patient " + Integer.toString(ID) + " leaves receptionist and sits down.");
					//full--;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						
						e.printStackTrace();
					}
					
					
					try {
						sHallway[dnum].acquire();
						//System.out.println("Patient " + Integer.toString(ID) + " calls for Nurse " + Integer.toString(dnum));
						folder = ID;
						n_Call[dnum].release();
						hGreenlight[dnum].acquire();
					} catch (InterruptedException e1) {
						
						e1.printStackTrace();
					} finally {
						sHallway[dnum].release();
					}
					try {
						sOffice[dnum].acquire();
						//System.out.println("Patient " + Integer.toString(ID) + " calls for Nurse " + Integer.toString(dnum));
						diagnosis = ID;
						//d_Call[dnum].release();
						oGreenlight[dnum].acquire();
					} catch (InterruptedException e1) {
						
						e1.printStackTrace();
					} finally {
						sOffice[dnum].release();
					}
					System.out.println("Patient " + Integer.toString(ID) + " listens to Doctor " + Integer.toString(dnum) + "'s advice.");
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					System.out.println("Patient " + Integer.toString(ID) + " leaves.");
				}
				
			});
			patients[i].start();
		}
		for(int i = 0; i < Integer.parseInt(args[1]); i++){
			doctors[i] = new Thread(new Runnable(){

				@Override
				public void run() {
					int ID = dCount;
					int pnum = 0;
					dCount++;
					
						try {
							while(d_Call[ID].tryAcquire(10, TimeUnit.SECONDS)){
								try {
									//d_Call[ID].acquire();
									//Thread.sleep(100);
									pnum = diagnosis;
									System.out.println("Doctor " + Integer.toString(ID) + " listens to symptoms from Patient " + Integer.toString(pnum));
								} /*catch (InterruptedException e) {
									e.printStackTrace();
								}*/ finally {
									oGreenlight[ID].release();
								}
								
								
								nd_Call[ID].release();
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Doctor " + Integer.toString(ID) + " goes home.");
					
				}});
			nurses[i] = new Thread(new Runnable(){

				@Override
				public void run() {
					int ID = nCount;
					int pnum = 0;
					nCount++;
						try {
							while(n_Call[ID].tryAcquire(10, TimeUnit.SECONDS)){
								try {
									//n_Call[ID].acquire();
									//Thread.sleep(100);
									pnum = folder;
									System.out.println("Nurse " + Integer.toString(ID) + " takes Patient " + Integer.toString(pnum) + " to Doctor " + Integer.toString(ID));
								}/* catch (InterruptedException e) {
									e.printStackTrace();
								}*/ finally{
									hGreenlight[ID].release();
									d_Call[ID].release();
								}
								try {
									nd_Call[ID].acquire();
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						System.out.println("Nurse " + Integer.toString(ID) + " goes home.");
				}});
		}
		for(int i = 0; i < Integer.parseInt(args[1]); i++){
			doctors[i].start();
			nurses[i].start();
		}
		
		Thread receptionist = new Thread(new Runnable() {
			public void run(){
				int pnum = 0;
				int ph = 0;
				try {
					while(sem.tryAcquire(1500, TimeUnit.MILLISECONDS)){
						try {
								//sem.acquire();
								//Thread.sleep(500);
								pnum = blackboard;
								assignment = ph = rand.nextInt(Integer.parseInt(args[1]));
								System.out.println("Receptionist registers Patient " + Integer.toString(pnum) + " to Doctor " + Integer.toString(ph));
							
						} finally{
							greenlight.release();
						}
						
						
						try {
						Thread.sleep(1000);
						} catch (InterruptedException e) {
							
							e.printStackTrace();
						}
						r_release.release();
					}
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("The Receptionist goes home.");
			}
		});
		
		receptionist.start();
	}

}
