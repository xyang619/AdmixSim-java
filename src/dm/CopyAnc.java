/*
 * AdmSimulator
 * CopyAnc.java
 * Copy Ancestral haplotype to the admixed haplotypes.
 */
package dm;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import adt.Chromosome;
import adt.Segment;

public class CopyAnc {
	
	public Vector<Double> readMap(String mapfile) {
		/*
		 * reading position for each locus in the genetic map file
		 * return a vector of position
		 */
		Vector<Double> position = new Vector<Double>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(mapfile));
			String line;
			while ((line = br.readLine()) != null) {
				double pos = Double.parseDouble(line);
				position.add(pos);
			}
			if (br != null) {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return position;
	}

	public Map<Integer, Vector<String>> readHaplo(String haplofile, Vector<Integer> initAnc) {
		/*
		 * reading ancestral haplotypes from file, and return a map with key as population label 
		 * and value as a vector of haplotypes
		 */
		Map<Integer, Vector<String>> anchaps = new HashMap<Integer, Vector<String>>();
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(haplofile));
			String line;
			int key = 1;
			Vector<String> haps = new Vector<String>();
			anchaps.put(key, haps);
			int nInd = 0;
			int nAnc = initAnc.elementAt(0);
			while ((line = br.readLine()) != null) {
				if (nInd < nAnc) {
					anchaps.get(key).add(line);
					nInd++;
				} else {
					key++;
					nInd = 0;
					nAnc = initAnc.elementAt(key - 1);
					haps = new Vector<String>();
					anchaps.put(key, haps);
					anchaps.get(key).add(line);
					nInd++;
				}
			}
			if (br != null) {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return anchaps;
	}

	public int indexOf(double pos, Vector<Double> position) {
		/*
		 * find the nearest genetic locus by given the genetic position
		 * binary search 
		 */
		if (position.size() == 0)
			return 0;
		if (pos <= position.firstElement())
			return 0;
		if (pos >= position.lastElement())
			return position.size();
		int left = 0;
		int right = position.size();
		int mid = (left + right + 1) / 2;
		while (left < right) {
			if (pos > position.elementAt(mid))
				left = mid;
			else
				right = mid - 1;
			mid = (left + right + 1) / 2;
		}
		if (Math.abs(pos - position.elementAt(mid)) > Math.abs(pos - position.elementAt(mid + 1)))
			return left + 1;
		else
			return left;
	}

	public String copy(Map<Integer, Vector<String>> anchaps, Vector<Double> pos, Chromosome chr) {
		/*
		 * copy the corresponding ancestry to fill in the segment,
		 * the label in segment are combined information with the digits larger than 4 digits as ancestry label,
		 * and the last four digits as the label from which haplotypes to copy
		 * for example 10005, indicates ancestry from ancestry 1 and copy from the 5th haplotype
		 */
		StringBuilder sb = new StringBuilder();
		for (Segment seg : chr.getSegments()) {
			int key = seg.getLabel() / 1000000;
			int ihap = seg.getLabel() % 1000000;
			int start = indexOf(seg.getStart(), pos);
			int end = indexOf(seg.getEnd(), pos);
			String tmp = anchaps.get(key).elementAt(ihap).substring(start, end);
			sb.append(tmp);
		}
		return sb.toString();
	}
}
