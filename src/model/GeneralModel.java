/*
 * AdmSimulator
 * GeneralModel.java
 * Population admixture can be generalized to K ancestral populations and T waves.
 * and the admixture can be fully modeled as TxK matrix, in which each element mij
 * denote the gene flow strength from jth ancestral population at ith generation
 * In each generation, the population size is N, then the number of individuals from
 * jth ancestral population is N*mij, same as all others, and the rests are sampled
 * from previous generation.
 */

package model;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.Vector;

import adt.ChromPair;
import adt.Chromosome;
import adt.Population;
import adt.Segment;

public class GeneralModel {
	private int[] initAnc;
	private int[] Nes;
	private double[][] props;
	private Population pop;
	Random random;

	public GeneralModel(String filename, int gen, int nanc, Random random) {
		readParams(filename, gen, nanc);
		this.random = random;
	}

	public GeneralModel(int[] Nes, double[][] props, Random random) {
		this.Nes = Nes;
		this.props = props;
		this.random = random;
	}

	public GeneralModel(int[] Nes, double[][] props, Population pop,
			Random random) {
		this.Nes = Nes;
		this.props = props;
		this.pop = pop;
		this.random = random;
	}

	public void readParams(String filename, int gen, int nanc) {
		Nes = new int[gen];
		props = new double[gen][nanc];
		BufferedReader br = null;
		boolean isStart = false;
		try {
			br = new BufferedReader(new FileReader(filename));
			String line;
			int i = 0;
			while ((line = br.readLine()) != null) {
				// remove comments
				if (line.startsWith("#")) {
					continue;
				}
				if (line.indexOf("#") != -1) {
					line = line.substring(0, line.indexOf("#"));
				}
				if (line.startsWith("//")) {
					isStart = true;
				}
				if (isStart && initAnc == null) {
					System.err.println("Uninitialized ancestral population number");
					System.exit(0);
				}
				String[] tmp = line.split("\\s+");
				if (!isStart && tmp.length >= nanc) {
					initAnc = new int[nanc];
					for (int j = 0; j < nanc; j++) {
						initAnc[j] = Integer.parseInt(tmp[j]);
					}
				}
				if (tmp.length > nanc && i < gen) {
					Nes[i] = Integer.parseInt(tmp[0]);
					for (int j = 0; j < nanc; j++) {
						props[i][j] = Double.parseDouble(tmp[j + 1]);
					}
					i++;
				}
			}
			if (br != null) {
				br.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public boolean isValidNe() {
		for (int ne : Nes) {
			if (ne <= 0) {
				System.err.println("Effective Population Size must be positive");
				return false;
			}
		}
		return true;
	}

	public boolean isValidProp() {
		for (int i = 0; i < props.length; i++) {
			double sum = 0;
			for (int j = 0; j < props[i].length; j++) {
				if (props[i][j] < 0 || props[i][j] > 1) {
					System.err.println("Admixture proportion must be between 0 and 1");
					return false;
				}
				sum += props[i][j];
			}
			if (i == 0 && sum != 1.0) {
				System.err.println("The proportion for initial generation must be 1");
				return false;
			}
			if (sum > 1) {
				System.err.println("The sum of proportion larger than 1");
				return false;
			}
		}
		return true;
	}

	public void evolve(double len) {
		if (!isValidNe() || !isValidProp()) {
			System.exit(1);
		}
		for (int i = 0; i < Nes.length; i++) {
			int numbIndsPrev = 0;
			int curNe = Nes[i];
			int numbAnc = props[i].length;
			int[] numbInds = new int[numbAnc];
			int sumNumbInds = 0;
			for (int j = 0; j < numbAnc; j++) {
				numbInds[j] = (int) (curNe * props[i][j]);
				sumNumbInds += numbInds[j];
			}
			// prepare individuals in current generation
			numbIndsPrev = curNe - sumNumbInds;
			Vector<ChromPair> indsCur = new Vector<ChromPair>();
			if (numbIndsPrev > 0 && pop != null) {
				indsCur = pop.sample(numbIndsPrev);
			}
			for (int j = 0; j < numbAnc; j++) {
				if (numbInds[j] > 0) {
					for (int k = 0; k < numbInds[j]; k++) {
						Vector<Segment> segs = new Vector<Segment>();
						// Label was used to distinguish segment from which
						// ancestral population and which haplotype in
						// ancestral population. The first number was set large
						// to distinguish ancestral population, and the rest was
						// used to distinguish haplotype
						int label = random.nextInt(initAnc[j]) + 10000 * (j + 1);
						segs.add(new Segment(0.0, len, label));
						Chromosome chr1 = new Chromosome(segs);
						segs = new Vector<Segment>();
						label = random.nextInt(initAnc[j]) + 10000 * (j + 1);
						segs.add(new Segment(0.0, len, label));
						Chromosome chr2 = new Chromosome(segs);
						indsCur.add(new ChromPair(chr1, chr2, random));
					}
				}
			}
			Population tmpPop = new Population(0, indsCur, random);
			pop = tmpPop.evolve(tmpPop.getNe());
		}

	}

	public int[] getInitAnc() {
		return initAnc;
	}

	public Population getPop() {
		return pop;
	}
}
