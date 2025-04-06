import { Categorie } from "../models/categorie.models";
import { ImageBassin } from "../models/image.models";

export class Bassin{
    idBassin!: number; 
    nomBassin!: string;
    description!: string;
    prix!: number;
   
    materiau!: string;
    couleur!: string;
    dimensions!: string;
    disponible!:boolean;
    stock!:number;

    categorie!: Categorie;

    image! : ImageBassin;
    imageStr!:string;

    imagesBassin!: ImageBassin[];

    image3DPath!:string;

}