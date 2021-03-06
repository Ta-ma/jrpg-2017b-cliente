package juego;

import java.awt.Graphics;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import entidades.Entidad;
import estados.Estado;
import mensajeria.PaqueteMovimiento;
import mensajeria.PaqueteNpc;
import mundo.Mundo;
import mundo.Tile;
import recursos.Recursos;

public class NpcManager
{
	//private final int CANTIDAD_NPCS = 10;
	
	// Esta clase se encarga del spawn y despawn de NPCs.
	
	// Distancia mínima desde el spawn de un npc hacia cualquier jugador: 416 (mas o menos)
	
	/* hay 3 maps:
	   paquetesNpcs: 	Paquetes que llevan la información de los npcs relacionada a stats.
	   
	   ubicacionNpcs: 	Paquetes que llevan la información sobre la posición de los npcs.
	   
	   esos paquetes están en juego debido a que deben ser sincronizados con el server.
	   
	   entidadesNpcs: 	Son las entidades que se instancian para cada npc y se encargan de dibujar
	   					en la pantalla el gráfico, de registrar los clicks para batallar etc.
	   					
	   Las entidades solamente son instanciadas del lado del cliente, mientras que los paquetes
	   se utilizaran luego para coordinar con el server para que todos vean los mismos npcs.
	   (eso todavía no está implementado pero es la idea)
	 */
	
	private Map<Integer, Entidad> entidadesNpcs;
	private Map<Integer, PaqueteNpc> paquetesNpcs;
	private Map<Integer, PaqueteMovimiento> ubicacionNpcs;
	private Juego juego;
	private Mundo mundo;
	
	/*
	 * Inicializa el NpcManager
	 * 
	 */ 
	public NpcManager (Juego juego, Mundo mundo)
	{
		entidadesNpcs = new HashMap<Integer, Entidad>();
		paquetesNpcs = juego.getPaquetesNpcs();
		ubicacionNpcs = juego.getUbicacionNpcs();
		this.juego = juego;
		this.mundo = mundo;
	}
	
	/*
	 * Aparece a un Npc individual.
	 */
	public void spawnNpc (int id, int nivel, String nombre, String raza, String casta,
			float posX, float posY, int dir)
	{
		float[] coords = new float[2];
		coords = Mundo.dosDaIso(posX, posY);
		
		paquetesNpcs.put(id, new PaqueteNpc(id, nivel, nombre, raza, casta));
		ubicacionNpcs.put(id, new PaqueteMovimiento(id, posX, posY, dir));
		
		Entidad ente = new Entidad(juego, mundo, 64, 64, nombre, coords[0], coords[1], Recursos.personaje.get(raza), 150);
		ente.setIdEnemigo(id);
		ente.setDireccion(dir);
		entidadesNpcs.put(id, ente);
	}
	
	/**
	 * Borra a un npc.
	 * @param id id del npc a borrar
	 * @return boolean falso si no lo pudo borrar
	 */
	
	public boolean despawnNpc (int id)
	{
		if(paquetesNpcs.remove(id) == null)
			return false;
		
		ubicacionNpcs.remove(id);
		entidadesNpcs.remove(id);
		
		return true;
	}
	
	/**
	 * Hace aparecer una determinada cantidad de npcs en el mapa.
	 * @param cant Cantidad de npcs a spawnear
	 */
	public void spawnInicial (int cant)
	{
		paquetesNpcs = new HashMap<Integer, PaqueteNpc>();
		ubicacionNpcs = new HashMap<Integer, PaqueteMovimiento>();
		
		juego.setPaquetesNpcs(this.paquetesNpcs);
		juego.setUbicacionNpcs(this.ubicacionNpcs);
		
		boolean puedoSpawnear;
		int posX, posY;
		//Tile tile;
		Random random = new Random();
		
		// Generación aleatoria
		String[] castas = {"Bandido", "Bruto", "Vampiro", "Brujo"};
		String[] razas = {"Humano", "Orco", "Elfo"};
		
		for (int i = 1; i <= cant; i++)
		{
			// determino una posición aleatoria para hacer aparecer al chobi
			do
			{
				// este hermoso algoritmo lo que hace básicamente es tirar una posición aleatoria
				// y luego se fija si la zona de 2 tiles a la redonda está totalmente despejada
				// Gracias Lucas por tanto, perdón por tan poco.
				puedoSpawnear = true;
				posX = random.nextInt(mundo.obtenerAncho() - 18) + 13;
				posY = random.nextInt(mundo.obtenerAlto() - 18) + 13;
				//tile = mundo.getTile(posX, posY);
				
				for(int j = -1; j < 2; j++)
				{
					for(int k = -1; k < 2; k++)
					{
						if (mundo.getTile(posX + j, posY + k).esSolido())
							puedoSpawnear = false;
					}
				}
				
			} while (!puedoSpawnear);
			
			spawnNpc(i, random.nextInt(10) + 1, generarNombre(), razas[random.nextInt(razas.length)], castas[random.nextInt(castas.length)], posX, posY, random.nextInt(8));
		}
	}
	
	/**
	 * Grafica los sprites de los npcs en el mundo.
	 * @param g Graphics del juego
	 */
	public void graficarNpcs(Graphics g) 
	{
		// recorro el árbol de entidades de los npcs y los voy graficando
		if(entidadesNpcs != null && !entidadesNpcs.isEmpty())
		{
			Iterator<Integer> it = entidadesNpcs.keySet().iterator();
			int key;
			Entidad actual;
			
			while (it.hasNext()) 
			{
				key = it.next();
				actual = entidadesNpcs.get(key);
				
				if(paquetesNpcs.get(key).getEstado() == Estado.estadoJuego)
					actual.graficar(g);
			}
		}
		
		/*for (Entidad actual : entidadesNpcs.values())
		{
			if(paquetesNpcs.get(actual.getIdEnemigo()).getEstado() == Estado.estadoJuego)
				actual.graficar(g);
		}*/
	}
	
	/**
	 * Grafica los nombres de los npcs en el mundo.
	 * @param g Graphics del juego
	 */
	public void graficarNombresNpcs(Graphics g) 
	{
		// recorro el árbol de entidades de los npcs y los voy graficando
		if(entidadesNpcs != null && !entidadesNpcs.isEmpty())
		{
			Iterator<Integer> it = entidadesNpcs.keySet().iterator();
			int key;
			Entidad actual;
			
			/*g.setColor(Color.WHITE);
			g.setFont(new Font("Book Antiqua", Font.PLAIN, 15));*/
			while (it.hasNext()) 
			{
				key = it.next();
				actual = entidadesNpcs.get(key);
				
				if(paquetesNpcs.get(key).getEstado() == Estado.estadoJuego)
					actual.graficarNombre(g);
			}
		}
	}

	public void actualizar()
	{
		entidadesNpcs = new HashMap<Integer, Entidad>();
		
		if(paquetesNpcs != null && !paquetesNpcs.isEmpty())
		{
			Iterator<Integer> it = paquetesNpcs.keySet().iterator();
			int key;
			PaqueteNpc actualNpc;
			PaqueteMovimiento actualUbicacion;
			
			/*g.setColor(Color.WHITE);
			g.setFont(new Font("Book Antiqua", Font.PLAIN, 15));*/
			while (it.hasNext()) 
			{
				key = it.next();
				actualNpc = paquetesNpcs.get(key);
				actualUbicacion = ubicacionNpcs.get(key);
				
				float[] coords = new float[2];
				coords = Mundo.dosDaIso(actualUbicacion.getPosX(), actualUbicacion.getPosY());
				
				//System.out.println(actualUbicacion.getDireccion());
				Entidad ente = new Entidad(juego, mundo, 64, 64, actualNpc.getNombre(), coords[0], coords[1], Recursos.personaje.get(actualNpc.getRaza()), 150);
				ente.setIdEnemigo(key);
				ente.setDireccion(actualUbicacion.getDireccion());
				entidadesNpcs.put(key, ente);
			}
		}
	}
	
	public Map<Integer, Entidad> getEntidadesNpcs()
	{
		return entidadesNpcs;
	}

	public void setEntidadesNpcs(Map<Integer, Entidad> entidadesNpcs)
	{
		this.entidadesNpcs = entidadesNpcs;
	}

	public String generarNombre()
	{
		Random random = new Random();
		
		String[] prefijos = {"Kaiser", "Sir", "Lord", "Commander", "Emperor", "Baron", "Duke",
							"Dauphin", "Count", "Prince", "Yeoman"};
		String[] nombres = {"Alton", "Dave", "William", "Vladimir", "Bradford", "Wilhelm", "Edmund",
							"Alexander", "Richard", "Greyson"};
		String[] sufijos = {"Destroyer", "Conqueror", "Impaler", "Butcher", "Ripper", "Soulless",
							"Ravager", "Streetcleaner", "Extremist", "Fanatic", "Sadist"};
		
		return prefijos[random.nextInt(prefijos.length)] + " " + nombres[random.nextInt(nombres.length)] + " the " + sufijos[random.nextInt(sufijos.length)];
	}
	
	/*public Map<Integer, PaqueteNpc> getPaquetesNpcs()
	{
		return paquetesNpcs;
	}

	public void setPaquetesNpcs(Map<Integer, PaqueteNpc> paquetesNpcs)
	{
		this.paquetesNpcs = paquetesNpcs;
	}

	public Map<Integer, PaqueteMovimiento> getUbicacionNpcs()
	{
		return ubicacionNpcs;
	}

	public void setUbicacionNpcs(Map<Integer, PaqueteMovimiento> ubicacionNpcs)
	{
		this.ubicacionNpcs = ubicacionNpcs;
	}*/
}
