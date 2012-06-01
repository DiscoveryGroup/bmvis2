package discovery.compression.kdd2011.old;
public class CompressorCosts {

	public double supernodes;
	public double labelCorrections;
	public double superedges;
	public double corrections;
	
	public CompressorCosts(){
		
	}
	public CompressorCosts(double a,double b,double c,double d){
		supernodes=a;
		labelCorrections=b;
		superedges=c;
		corrections=d;
	}
	public String toString(){
		return supernodes+" "+labelCorrections+" "+superedges+" "+corrections+" "+
			(supernodes+labelCorrections+superedges+corrections);
	}
}
